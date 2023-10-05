package practicum.compilations.service;

import practicum.compilations.dto.CompilationDto;
import practicum.compilations.dto.CompilationUpdatedDto;
import practicum.compilations.dto.NewCompilationDto;

import java.util.List;

public interface CompilationService {

    CompilationDto createCompilationAdmin(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilationByIdAdmin(Long compId, CompilationUpdatedDto updateCompilationRequest);

    void deleteCompilationByIdAdmin(Long compId);

    List<CompilationDto> getAllCompilationsPublic(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationByIdPublic(Long id);
}