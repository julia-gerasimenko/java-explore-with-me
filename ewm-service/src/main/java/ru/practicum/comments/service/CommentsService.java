package ru.practicum.comments.service;


import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;

import java.util.List;

public interface CommentsService {
    CommentDto createCommentPrivate(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateCommentByIdPrivate(Long userId, Long commentId, NewCommentDto newCommentDto);

    CommentDto getCommentByIdPrivate(Long userId, Long commentId);

    void deleteCommentByIdPrivate(Long userId, Long commentId);

    CommentDto getCommentByIdAdmin(Long commentId);

    List<CommentDto> getCommentsByAuthorIdPrivate(Long userId, Integer from, Integer size);

    List<CommentDto> getCommentsByEventIdPrivate(Long userId, Long eventId, Integer from, Integer size);

    CommentDto updateCommentAdmin(Long commentId, NewCommentDto newCommentDto);

    List<CommentDto> getCommentsPublic(Long eventId, String text, Integer from, Integer size);

    void deleteCommentByIdAdmin(Long commentId);
}