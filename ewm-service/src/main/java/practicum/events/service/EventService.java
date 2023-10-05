package practicum.events.service;

import practicum.events.dto.EventFullDto;
import practicum.events.dto.EventShortDto;
import practicum.events.dto.EventUpdatedDto;
import practicum.events.dto.NewEventDto;
import practicum.util.enam.EventState;
import practicum.util.enam.EventsSort;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventFullDto> getAllEventsAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEventByIdAdmin(Long eventId, EventUpdatedDto eventDto);

    EventFullDto createEventPrivate(Long userId, NewEventDto eventDto);

    List<EventShortDto> getAllEventsByUserIdPrivate(Long userId, int from, int size);

    EventFullDto getEventByIdPrivate(Long userId, Long eventId);

    EventFullDto updateEventByIdPrivate(Long userId, Long eventId, EventUpdatedDto eventDto);

    List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, Boolean onlyAvailable, EventsSort sort, Integer from,
                                        Integer size, HttpServletRequest request);

    EventFullDto getEventByIdPublic(Long id, HttpServletRequest request);
}