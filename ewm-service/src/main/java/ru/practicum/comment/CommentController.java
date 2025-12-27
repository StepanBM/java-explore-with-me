package ru.practicum.comment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentShortDto;
import ru.practicum.comment.dto.NewCommentRequest;
import ru.practicum.comment.dto.UpdateCommentRequest;
import ru.practicum.exceptions.CreateValidation;
import ru.practicum.exceptions.UpdateValidation;

import java.util.List;

@Slf4j
@RestController
@RequestMapping
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // Добавлять комментарий может только авторезированный пользователь к любому событию
    @PostMapping("/users/{userId}/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long userId,
                                 @PathVariable Long eventId,
                                 @Validated(CreateValidation.class) @RequestBody NewCommentRequest request) {

        log.info("Добавление нового коммаентария");
        return commentService.addComment(userId, eventId, request);
    }

    // Вывод определенного комментария по id
    @GetMapping("/comments/{commentId}")
    public CommentShortDto getCommentId(@PathVariable Long commentId) {

        log.info("Вывод коммаентария с id={}", commentId);
        return commentService.getCommentId(commentId);
    }

    // Вывод всех комментариев к определенному событию
    @GetMapping("/events/{eventId}/comments")
    public List<CommentShortDto> getAllCommentsEvent(@PathVariable Long eventId,
                                                    @RequestParam(defaultValue = "0") int from,
                                                    @RequestParam(defaultValue = "10") int size) {

        log.info("Вывод коммаентариев события с id={}", eventId);
        return commentService.getAllCommentsEvent(eventId, from, size);
    }

    // Вывод всех комментариев к определенного пользователя
    @GetMapping("/users/{userId}/comments")
    public List<CommentShortDto> getAllCommentsUser(@PathVariable Long userId,
                                                   @RequestParam(defaultValue = "0") int from,
                                                   @RequestParam(defaultValue = "10") int size) {

        log.info("Вывод коммаентариев пользователя с id={}", userId);
        return commentService.getAllCommentsUser(userId, from, size);
    }

    // Обновлять комментарий может только тот пользователь который его оставил
    @PatchMapping("/users/{userId}/comments/{commentId}")
    public CommentDto updateCommentUser(@PathVariable Long userId,
                                    @PathVariable Long commentId,
                                    @Validated(UpdateValidation.class) @RequestBody UpdateCommentRequest request) {

        log.info("Запрос на обновление комментария пользователем: пользователь id={}, комментарий id={}, данные={}", userId, commentId, request);
        return commentService.updateCommentUser(userId, commentId, request);
    }

    // Админ может обновить любой комментарий любого пользователя
    @PatchMapping("/admin/comments/{commentId}")
    public CommentDto updateCommentAdmin(@PathVariable Long commentId,
                                         @Validated(UpdateValidation.class) @RequestBody UpdateCommentRequest request) {

        log.info("Запрос на обновление комментария админом: комментарий id={}, данные={}", commentId, request);
        return commentService.updateCommentAdmin(commentId, request);
    }


    // Уадалять комментарий может только тот пользователь который его оставил
    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentUser(@PathVariable Long userId, @PathVariable Long commentId) {

        log.info("Запрос на удаление комментария пользователем: пользователь id={}, комментарий id={}", userId, commentId);
        commentService.deleteCommentUser(userId, commentId);
    }

    // Админ может удалять любой комментарий
    @DeleteMapping("/admin/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentAdmin(@PathVariable Long commentId) {

        log.info("Запрос на обновление комментария админом: комментарий id={}", commentId);
        commentService.deleteCommentAdmin(commentId);
    }

}
