package ru.practicum.comment;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentShortDto;
import ru.practicum.comment.dto.NewCommentRequest;
import ru.practicum.comment.dto.UpdateCommentRequest;

import java.util.List;

public interface CommentService {

    CommentDto addComment(Long userId, Long eventId, NewCommentRequest request);

    CommentShortDto getCommentId(Long commentId);

    List<CommentShortDto> getAllCommentsEvent(Long eventId, int from, int size);

    List<CommentShortDto> getAllCommentsUser(Long userId, int from, int size);

    CommentDto updateCommentUser(Long userId, Long commentId, UpdateCommentRequest request);

    CommentDto updateCommentAdmin(Long commentId, UpdateCommentRequest request);

    void deleteCommentUser(Long userId, Long commentId);

    void deleteCommentAdmin(Long commentId);
}
