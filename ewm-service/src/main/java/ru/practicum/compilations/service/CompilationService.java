package ru.practicum.compilations.service;

import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.CompilationUpdatedDto;

import java.util.List;

public interface CompilationService {

    CompilationDto createCompilationAdmin(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilationByIdAdmin(Long compId, CompilationUpdatedDto updateCompilationRequest);

    List<CompilationDto> getAllCompilationsPublic(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationByIdPublic(Long id);
    void deleteCompilationByIdAdmin(Long compId);
}