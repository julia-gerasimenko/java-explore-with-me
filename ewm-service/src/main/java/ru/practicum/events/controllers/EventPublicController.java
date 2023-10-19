package ru.practicum.events.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.util.enam.EventsSort;
import ru.practicum.events.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static ru.practicum.util.Constants.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EventPublicController {

    private final EventService eventService;

    @GetMapping("/{id}")
    public EventFullDto getEventByIdPublic(@PathVariable(value = "id") Long id,
                                           HttpServletRequest request) {
        log.info("Get event with id= {}", id);
        return eventService.getEventByIdPublic(id, request);
    }

    @GetMapping
    public Collection<EventShortDto> getEventsPublic(@RequestParam(required = false) String text,
                                                     @RequestParam(required = false) List<Long> categories,
                                                     @RequestParam(required = false) Boolean paid,
                                                     @RequestParam(required = false)
                                                     @DateTimeFormat(pattern = DATE_DEFAULT) LocalDateTime rangeStart,
                                                     @RequestParam(required = false)
                                                     @DateTimeFormat(pattern = DATE_DEFAULT) LocalDateTime rangeEnd,
                                                     @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                     @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                                     @RequestParam(defaultValue = PAGE_DEFAULT_FROM)
                                                     @PositiveOrZero Integer from,
                                                     @RequestParam(defaultValue = PAGE_DEFAULT_SIZE)
                                                     @Positive Integer size,
                                                     HttpServletRequest request
    ) {
        EventsSort sortParam = EventsSort.from(sort).orElseThrow(() -> new ValidationException("Sort isn't valid: "
                + sort));

        log.info("Get public events with text {}, categories {} onlyAvailable {}, sort {}", text, categories,
                onlyAvailable, sort);
        return eventService.getEventsPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sortParam, from, size, request);
    }
}
