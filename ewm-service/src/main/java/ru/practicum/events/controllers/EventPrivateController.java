package ru.practicum.events.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.EventUpdatedDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.service.EventService;
import ru.practicum.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

import static ru.practicum.util.Constants.PAGE_DEFAULT_FROM;
import static ru.practicum.util.Constants.PAGE_DEFAULT_SIZE;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EventPrivateController {

    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public EventFullDto create(@PathVariable(value = "userId") Long userId,
                               @Valid @RequestBody NewEventDto eventDto) {
        return eventService.createEventPrivate(userId, eventDto);
    }


    @GetMapping
    public Collection<EventShortDto> getEventsByUserId(@PathVariable(value = "userId") Long userId,
                                                       @RequestParam(value = "from", defaultValue = PAGE_DEFAULT_FROM)
                                                       @PositiveOrZero Integer from,
                                                       @RequestParam(value = "size", defaultValue = PAGE_DEFAULT_SIZE)
                                                       @Positive Integer size) {
        return eventService.getAllEventsByUserIdPrivate(userId, from, size);
    }

    @GetMapping("/{eventId}/requests")
    public Collection<ParticipationRequestDto> getParticipationRequest(@PathVariable(value = "userId") Long userId,
                                                                       @PathVariable(value = "eventId") Long eventId) {
        return requestService.getParticipationRequestPrivate(userId, eventId);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable(value = "userId") Long userId,
                                     @PathVariable(value = "eventId") Long eventId) {
        return eventService.getEventByIdPrivate(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable(value = "userId") Long userId,
                                    @PathVariable(value = "eventId") Long eventId,
                                    @Valid @RequestBody EventUpdatedDto eventDto) {
        return eventService.updateEventByIdPrivate(userId, eventId, eventDto);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequestStatus(@PathVariable(value = "userId") Long userId,
                                                                   @PathVariable(value = "eventId") Long eventId,
                                                                   @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return requestService.updateEventRequestStatusPrivate(userId, eventId, updateRequest);
    }
}