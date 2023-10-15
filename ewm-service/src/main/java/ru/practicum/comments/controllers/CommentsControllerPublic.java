package ru.practicum.comments.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.service.CommentsService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.util.Constants.PAGE_DEFAULT_FROM;
import static ru.practicum.util.Constants.PAGE_DEFAULT_SIZE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
@Validated
@Slf4j
public class CommentsControllerPublic {

    private final CommentsService commentsService;

    @GetMapping
    public List<CommentDto> getByTextPublic(@RequestParam(value = "eventId") Long eventId,
                                            @RequestParam(required = false) String text,
                                            @RequestParam(defaultValue = PAGE_DEFAULT_FROM)
                                            @PositiveOrZero Integer from,
                                            @RequestParam(defaultValue = PAGE_DEFAULT_SIZE)
                                            @Positive Integer size
    ) {
        return commentsService.getCommentsPublic(eventId, text, from, size);
    }
}