package ru.practicum.comment;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentShortDto;
import ru.practicum.comment.dto.NewCommentRequest;
import ru.practicum.comment.dto.UpdateCommentRequest;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.exceptions.ConflictErrorException;
import ru.practicum.exceptions.EventNotPublishedException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.state.EventState;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;
import ru.practicum.user.dto.UserShortDto;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.comment.CommentMapper.*;
import static ru.practicum.event.EventMapper.mapToEventShortDto;
import static ru.practicum.user.UserMapper.mapToUserShortDto;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                            EventRepository eventRepository,
                            UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске пользователя. Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден");
                });

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске события. Событие с id={} не найдено", eventId);
                    return new NotFoundException("Событие с id=" + eventId + " не найдено");
                });

        // Проверка, что событие опубликованно
        if (event.getState() != EventState.PUBLISHED) {
            log.warn("Ошибка при поиске события. Событие с id={} не еще не опубликованно", eventId);
            throw new EventNotPublishedException("Событие с id=" + eventId + " не еще не опубликованно");
        }

        // Проверка повторного комментария
        if (commentRepository.existsByEventAndAuthor(event, user)) {
            log.warn("Ошибка при выставлении комментария. К данному событию с eventId={} от пользователя с userId={} уже есть комментарий", eventId, userId);
            throw new ConflictErrorException("У данного события уже есть комментарий от данного пользователя");
        }

        log.debug("Начинается добавление комментария");
        Comment comment = mapToComment(request, event, user);
        comment = commentRepository.save(comment);

        log.debug("Добавлен комментарий {}", comment);
        return mapToCommentDto(comment);

    }

    @Override
    public CommentShortDto getCommentId(Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске комментария. Комментарий с id={} не найден", commentId);
                    return new NotFoundException("Комментарий с id=" + commentId + " не найден");
                });

        UserShortDto userShortDto = mapToUserShortDto(comment.getAuthor());
        EventShortDto eventShortDto = mapToEventShortDto(comment.getEvent());

        return mapToCommentShortDto(comment, eventShortDto, userShortDto);
    }

    @Override
    public List<CommentShortDto> getAllCommentsEvent(Long eventId, int from, int size) {
        log.info("Начало получения комментариев события с id={}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске события. Событие с id={} не найдено", eventId);
                    return new NotFoundException("Событие с id=" + eventId + " не найдено");
                });

        // Сортируем по дате создания в порядке убывания (первые идут новые комментарии)
        Sort sort = Sort.by(Sort.Direction.DESC, "created");

        Pageable pageable = PageRequest.of(from / size, size, sort);

        // Получение комментариев по eventId
        Page<Comment> commentsPage = commentRepository.findByEventIdOrderByCreatedDesc(eventId, pageable);

        List<Comment> comments = commentsPage.getContent();

        List<CommentShortDto> commentList = new ArrayList<>();

        for (Comment comment : comments) {
            UserShortDto authorDto = mapToUserShortDto(comment.getAuthor());
            EventShortDto eventDto = mapToEventShortDto(comment.getEvent());
            CommentShortDto commentDto = mapToCommentShortDto(comment, eventDto, authorDto);
            commentList.add(commentDto);
            log.debug("Обработан комментарий id={}, текст='{}'", comment.getId(), comment.getText());
        }

        log.debug("Обработка комментариев завершена. Всего обработано: {}", commentList.size());
        return commentList;

    }

    @Override
    public List<CommentShortDto> getAllCommentsUser(Long userId, int from, int size) {
        log.info("Начало получения комментариев пользователя с id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске пользователя. Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден");
                });

        Sort sort = Sort.by(Sort.Direction.DESC, "created");

        Pageable pageable = PageRequest.of(from / size, size, sort);

        // Получение комментариев по userId
        Page<Comment> commentsPage = commentRepository.findByAuthorId(userId, pageable);

        List<Comment> comments = commentsPage.getContent();

        List<CommentShortDto> commentList = new ArrayList<>();

        for (Comment comment : comments) {
            UserShortDto authorDto = mapToUserShortDto(comment.getAuthor());
            EventShortDto eventDto = mapToEventShortDto(comment.getEvent());
            CommentShortDto commentDto = CommentMapper.mapToCommentShortDto(comment, eventDto, authorDto);
            commentList.add(commentDto);
            log.debug("Обработан комментарий id={}, текст='{}'", comment.getId(), comment.getText());
        }

        log.debug("Обработка комментариев завершена. Всего обработано: {}", commentList.size());
        return commentList;

    }

    @Override
    @Transactional
    public CommentDto updateCommentUser(Long userId, Long commentId, UpdateCommentRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске пользователя. Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден");
                });

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске комментария. Комментарий с id={} не найден", commentId);
                    return new NotFoundException("Комментарий с id=" + commentId + " не найден");
                });

        // Проверка принадлежности комментария
        if (comment.getAuthor() != user) {
            log.warn("Ошибка при выставлении комментария. Данный пользователь с userId={} не выставлял комментарий к данному событию", userId);
            throw new ConflictErrorException("Данный пользователь не выставлял комментарий к данному событию");
        }

        updateCommentFields(comment, request);

        Comment updatedComment = commentRepository.save(comment);

        log.debug("Обновлен пользователем комментарий {}", comment);
        return mapToCommentDto(updatedComment);
    }

    @Override
    @Transactional
    public CommentDto updateCommentAdmin(Long commentId, UpdateCommentRequest request) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске комментария. Комментарий с id={} не найден", commentId);
                    return new NotFoundException("Комментарий с id=" + commentId + " не найден");
                });

        updateCommentFields(comment, request);

        Comment updatedComment = commentRepository.save(comment);

        log.debug("Обновлен админом комментарий {}", comment);
        return mapToCommentDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteCommentUser(Long userId, Long commentId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске пользователя. Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден");
                });

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске комментария. Комментарий с id={} не найден", commentId);
                    return new NotFoundException("Комментарий с id=" + commentId + " не найден");
                });

        // Проверка принадлежности комментария
        if (comment.getAuthor() != user) {
            log.warn("Ошибка при выставлении комментария. Данный пользователь с userId={} не выставлял комментарий к данному событию", userId);
            throw new ConflictErrorException("Данный пользователь не выставлял комментарий к данному событию");
        }

        commentRepository.delete(comment);
        log.debug("Комментарий {} успешно удалён.", commentId);
    }

    @Override
    @Transactional
    public void deleteCommentAdmin(Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске комментария. Комментарий с id={} не найден", commentId);
                    return new NotFoundException("Комментарий с id=" + commentId + " не найден");
                });

        commentRepository.delete(comment);
        log.debug("Комментарий {} успешно удалён.", commentId);
    }

}
