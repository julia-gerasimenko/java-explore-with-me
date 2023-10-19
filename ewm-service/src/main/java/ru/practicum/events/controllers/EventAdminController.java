package ru.practicum.events.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.util.enam.EventState;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventUpdatedDto;
import ru.practicum.events.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static ru.practicum.util.Constants.*;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EventAdminController {

    private final EventService eventService;

    @GetMapping
    public Collection<EventFullDto> getEvents(@RequestParam(required = false) List<Long> users,
                                              @RequestParam(required = false) List<EventState> states,
                                              @RequestParam(required = false) List<Long> categories,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(pattern = DATE_DEFAULT) LocalDateTime rangeStart,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(pattern = DATE_DEFAULT) LocalDateTime rangeEnd,
                                              @RequestParam(defaultValue = PAGE_DEFAULT_FROM)
                                              @PositiveOrZero Integer from,
                                              @RequestParam(defaultValue = PAGE_DEFAULT_SIZE)
                                              @Positive Integer size) {
        log.info("Get events of users {} with states {}, categories {}", users, states, categories);
        return eventService.getAllEventsAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAdmin(@PathVariable(value = "eventId") Long eventId,
                                         @Valid @RequestBody EventUpdatedDto eventDto) {
        log.info("Update event {} with id= {}", eventDto, eventId);
        return eventService.updateEventByIdAdmin(eventId, eventDto);
    }
}