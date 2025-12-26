package ru.practicum.comment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentShortDto;
import ru.practicum.comment.dto.NewCommentRequest;
import ru.practicum.comment.dto.UpdateCommentRequest;
import ru.practicum.event.Event;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.user.User;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.event.EventMapper.mapToEventDto;
import static ru.practicum.user.UserMapper.mapToUserDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static Comment mapToComment(NewCommentRequest request, Event event, User author) {
        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setAuthor(author);
        comment.setEvent(event);

        // Устанавливаем начальные значения
        comment.setCreated(LocalDateTime.now());

        return comment;
    }

    public static CommentDto mapToCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthor(mapToUserDto(comment.getAuthor()));
        dto.setEvent(mapToEventDto(comment.getEvent()));

        dto.setCreated(comment.getCreated());

        return dto;
    }

    public static CommentShortDto mapToCommentShortDto(Comment comment, EventShortDto event, UserShortDto author) {
        CommentShortDto dto = new CommentShortDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthor(author);
        dto.setEvent(event);

        dto.setCreated(LocalDateTime.now());

        return dto;
    }

    public static void updateCommentFields(Comment comment, UpdateCommentRequest request) {
        if (request.hasText()) {
            comment.setText(request.getText());
        }
    }
}
