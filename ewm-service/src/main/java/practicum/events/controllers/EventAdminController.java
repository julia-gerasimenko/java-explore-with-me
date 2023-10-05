package practicum.events.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import practicum.events.dto.EventFullDto;
import practicum.events.dto.EventUpdatedDto;
import practicum.events.service.EventService;
import practicum.util.enam.EventState;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static practicum.util.Constants.*;

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
        return eventService.getAllEventsAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAdmin(@PathVariable(value = "eventId") Long eventId,
                                         @Valid @RequestBody EventUpdatedDto eventDto) {
        return eventService.updateEventByIdAdmin(eventId, eventDto);
    }

}