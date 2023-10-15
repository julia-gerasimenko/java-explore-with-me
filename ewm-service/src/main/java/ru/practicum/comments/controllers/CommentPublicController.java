package ru.practicum.comments.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.service.CommentService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.util.Constants.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CommentPublicController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getByTextPublic(@RequestParam(value = "eventId") Long eventId,
                                            @RequestParam(required = false) String text,
                                            @RequestParam(defaultValue = PAGE_DEFAULT_FROM)
                                            @PositiveOrZero Integer from,
                                            @RequestParam(defaultValue = PAGE_DEFAULT_SIZE)
                                            @Positive Integer size
    ) {
        return commentService.getCommentsPublic(eventId, text, from, size);
    }
}