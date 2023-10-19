package ru.practicum.compilations.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.service.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

import static ru.practicum.util.Constants.PAGE_DEFAULT_FROM;
import static ru.practicum.util.Constants.PAGE_DEFAULT_SIZE;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CompilationPublicController {

    private final CompilationService serviceCompilation;

    @GetMapping("/{compId}")
    public CompilationDto getByIdPublic(@PathVariable Long compId) {
        log.info("Get compilation with id= {}", compId);
        return serviceCompilation.getCompilationByIdPublic(compId);
    }

    @GetMapping
    public Collection<CompilationDto> get(@RequestParam(required = false) Boolean pinned,
                                          @RequestParam(defaultValue = PAGE_DEFAULT_FROM)
                                          @PositiveOrZero Integer from,
                                          @RequestParam(defaultValue = PAGE_DEFAULT_SIZE)
                                          @Positive Integer size) {
        log.info("Get compilations pinned {}", pinned);
        return serviceCompilation.getAllCompilationsPublic(pinned, from, size);
    }

}