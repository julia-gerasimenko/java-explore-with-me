package ru.practicum.compilations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilations.dto.CompilationUpdatedDto;
import ru.practicum.events.model.Event;
import ru.practicum.util.Pagination;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.CompilationMapper;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.model.Compilation;
import ru.practicum.compilations.repository.CompilationRepository;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.handler.NotFoundException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.compilations.dto.CompilationMapper.mapToNewCompilation;
import static ru.practicum.compilations.dto.CompilationMapper.mapToCompilationDto;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto createCompilationAdmin(NewCompilationDto compilationDto) {
        Compilation compilation = mapToNewCompilation(compilationDto);

        if (compilationDto.getPinned() != null) {
            compilation.setPinned(compilationDto.getPinned());
        } else {
            compilation.setPinned(false);
        }

        Set<Long> eventsId = compilationDto.getEvents();
        if (eventsId != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllByIdIn(eventsId));
            compilation.setEvents(events);
        }

        Compilation savedCompilation = compilationRepository.save(compilation);

        return mapToCompilationDto(savedCompilation);
    }


    @Override
    public CompilationDto updateCompilationByIdAdmin(Long compId, CompilationUpdatedDto dto) {
        Compilation toUpdate = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException(String.format("Компиляция с id %s не найдена.", compId)));

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            toUpdate.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            toUpdate.setPinned(dto.getPinned());
        }

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            Set<Long> eventsId = dto.getEvents();
            Collection<Event> events = eventRepository.findAllByIdIn(eventsId);
            toUpdate.setEvents(new HashSet<>(events));
        }

        return mapToCompilationDto(toUpdate);
    }


    @Transactional(readOnly = true)
    @Override
    public List<CompilationDto> getAllCompilationsPublic(Boolean pinned, Integer from, Integer size) {
        if (pinned == null) {
            return compilationRepository.findAll(new Pagination(from, size, Sort.unsorted())).getContent().stream()
                    .map(CompilationMapper::mapToCompilationDto)
                    .collect(Collectors.toList());
        }

        log.info("Получены все компиляции: pinned = {}, from = {}, size = {}", pinned, from, size);

        return compilationRepository.findAllByPinned(pinned, new Pagination(from, size, Sort.unsorted()))
                .getContent().stream()
                .map(CompilationMapper::mapToCompilationDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public CompilationDto getCompilationByIdPublic(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Компиляция с compId = " + compId + " не найдена."));

        log.info("Компиляция с id = {} получена.", compId);
        return mapToCompilationDto(compilation);
    }

    @Override
    public void deleteCompilationByIdAdmin(Long compId) {
        if (compilationRepository.findById(compId).isEmpty()) {
            throw new NotFoundException(String.format("Компиляция с id %s не найдена.", compId));
        }

        compilationRepository.deleteById(compId);
        log.info("Компиляция с id= {} удалена.", compId);
    }

    @Transactional(readOnly = true)
    private Compilation getCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Компиляция с compId = " + compId + " не найдена."));
    }
}