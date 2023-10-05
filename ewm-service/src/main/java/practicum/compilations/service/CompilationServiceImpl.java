package practicum.compilations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practicum.compilations.dto.CompilationDto;
import practicum.compilations.dto.CompilationMapper;
import practicum.compilations.dto.CompilationUpdatedDto;
import practicum.compilations.dto.NewCompilationDto;
import practicum.compilations.model.Compilation;
import practicum.compilations.repository.CompilationRepository;
import practicum.events.model.Event;
import practicum.events.repository.EventRepository;
import practicum.handler.NotFoundException;
import practicum.util.Pagination;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static practicum.compilations.dto.CompilationMapper.mapToCompilationDto;
import static practicum.compilations.dto.CompilationMapper.mapToNewCompilation;

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

    @Override
    public void deleteCompilationByIdAdmin(Long compId) {
        getCompilation(compId);
        log.info("Компиляция с id= {} удалена.", compId);
        compilationRepository.deleteById(compId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompilationDto> getAllCompilationsPublic(Boolean pinned, Integer from, Integer size) {
        log.info("Получены все компиляции");
        if (pinned == null) {
            return compilationRepository.findAll(new Pagination(from, size, Sort.unsorted())).getContent().stream()
                    .map(CompilationMapper::mapToCompilationDto)
                    .collect(Collectors.toList());
        }

        return compilationRepository.findAllByPinned(pinned, new Pagination(from, size, Sort.unsorted()))
                .getContent().stream()
                .map(CompilationMapper::mapToCompilationDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public CompilationDto getCompilationByIdPublic(Long id) {
        Compilation compilation = getCompilation(id);
        log.info("Компиляция с id = {} получена.", id);
        return mapToCompilationDto(compilation);
    }

    @Transactional(readOnly = true)
    private Compilation getCompilation(Long id) {
        return compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Компиляция с id = " + id + " не найдена."));
    }
}