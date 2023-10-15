package ru.practicum.events.service;

import ru.practicum.util.enam.EventStates;
import ru.practicum.util.enam.EventsSorting;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.EventUpdatedDto;
import ru.practicum.events.dto.NewEventDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventFullDto> getAllEventsAdmin(List<Long> users, List<EventStates> states, List<Long> categories,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEventByIdAdmin(Long eventId, EventUpdatedDto eventDto);

    EventFullDto updateEventByIdPrivate(Long userId, Long eventId, EventUpdatedDto eventDto);

    EventFullDto createEventPrivate(Long userId, NewEventDto eventDto);

    List<EventShortDto> getAllEventsByUserIdPrivate(Long userId, int from, int size);

    EventFullDto getEventByIdPrivate(Long userId, Long eventId);

        List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, Boolean onlyAvailable, EventsSorting sort, Integer from,
                                            Integer size, HttpServletRequest request);

    EventFullDto getEventByIdPublic(Long id, HttpServletRequest request);
}