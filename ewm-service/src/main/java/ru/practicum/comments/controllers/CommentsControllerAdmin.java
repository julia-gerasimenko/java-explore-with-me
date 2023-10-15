package ru.practicum.comments.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.service.CommentsService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
@Slf4j
public class CommentsControllerAdmin {

    private final CommentsService commentsService;

    @GetMapping("/{commentId}")
    public CommentDto getByIdAdmin(@PathVariable(value = "commentId") Long commentId) {
        return commentsService.getCommentByIdAdmin(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateAdmin(@PathVariable(value = "commentId") Long commentId,
                                  @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentsService.updateCommentAdmin(commentId, newCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByIdAdmin(@PathVariable(value = "commentId") Long commentId) {
        commentsService.deleteCommentByIdAdmin(commentId);
    }
}