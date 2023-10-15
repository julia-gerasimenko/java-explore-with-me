package ru.practicum.comments.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.util.Constants.PAGE_DEFAULT_FROM;
import static ru.practicum.util.Constants.PAGE_DEFAULT_SIZE;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CommentPrivateController {

    private final CommentService commentService;

    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createdComment(@PathVariable(value = "userId") Long userId,
                                     @RequestParam(value = "eventId") Long eventId,
                                     @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.createCommentPrivate(userId, eventId, newCommentDto);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto updateComment(@PathVariable(value = "userId") Long userId,
                                    @PathVariable(value = "commentId") Long commentId,
                                    @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.updateCommentByIdPrivate(userId, commentId, newCommentDto);
    }

    @GetMapping("/comments")
    public List<CommentDto> getCommentByUser(@PathVariable(value = "userId") Long userId,
                                             @RequestParam(defaultValue = PAGE_DEFAULT_FROM)
                                             @PositiveOrZero Integer from,
                                             @RequestParam(defaultValue = PAGE_DEFAULT_SIZE)
                                             @Positive Integer size) {
        return commentService.getCommentsByAuthorIdPrivate(userId, from, size);
    }

    @GetMapping("/comments/{commentId}")
    public CommentDto getCommentById(@PathVariable(value = "userId") Long userId,
                                     @PathVariable(value = "commentId") Long commentId) {
        return commentService.getCommentByIdPrivate(userId, commentId);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getCommentsByEventId(@PathVariable(value = "userId") Long userId,
                                                 @PathVariable(value = "eventId") Long eventId,
                                                 @RequestParam(defaultValue = PAGE_DEFAULT_FROM)
                                                 @PositiveOrZero Integer from,
                                                 @RequestParam(defaultValue = PAGE_DEFAULT_SIZE)
                                                 @Positive Integer size) {
        return commentService.getCommentsByEventIdPrivate(userId, eventId, from, size);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable(value = "userId") Long userId,
                              @PathVariable(value = "commentId") Long commentId) {
        commentService.deleteCommentByIdPrivate(userId, commentId);
    }
}