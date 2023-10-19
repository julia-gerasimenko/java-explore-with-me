package ru.practicum.compilations.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.CompilationUpdatedDto;
import ru.practicum.compilations.service.CompilationService;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminController {

    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Create compilation {}", newCompilationDto);
        return compilationService.createCompilationAdmin(newCompilationDto);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateById(@PathVariable(value = "compId") Long compId,
                                     @Valid @RequestBody CompilationUpdatedDto compilationDto) {
        log.info("Update compilation {} with id = {}", compilationDto, compId);
        return compilationService.updateCompilationByIdAdmin(compId, compilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(value = "compId") Long compId) {
        log.info("Delete compilation  with id = {}", compId);
        compilationService.deleteCompilationByIdAdmin(compId);
    }
}