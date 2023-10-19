package ru.practicum.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.comments.dto.CommentCountDto;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.locations.model.Location;
import ru.practicum.locations.repository.LocationRepository;
import ru.practicum.requests.model.ParticipationRequest;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.util.enam.EventRequestStatus;
import ru.practicum.util.enam.EventState;
import ru.practicum.util.enam.EventsSort;
import ru.practicum.util.Pagination;
import ru.practicum.StatsClient;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.util.enam.EventStateAction;
import ru.practicum.events.dto.*;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.handler.NotFoundException;
import ru.practicum.handler.ValidateException;
import ru.practicum.handler.ValidateDateException;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.events.dto.EventMapper.*;
import static ru.practicum.events.dto.EventMapper.mapToEventFullDtoWithComments;
import static ru.practicum.locations.dto.LocationMapper.mapToLocation;
import static ru.practicum.util.enam.EventsSort.EVENT_DATE;
import static ru.practicum.util.enam.EventsSort.VIEWS;
import static ru.practicum.util.Constants.*;
import static ru.practicum.util.enam.EventState.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
@PropertySource(value = {"classpath:application.properties"})
public class EventServiceImpl implements EventService {
    @Value("${app}")
    private String app;

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    private final LocationRepository locationRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<EventFullDto> getAllEventsAdmin(List<Long> users,
                                                List<EventState> states,
                                                List<Long> categories,
                                                LocalDateTime rangeStart,
                                                LocalDateTime rangeEnd,
                                                Integer from,
                                                Integer size) {

        validDateParam(rangeStart, rangeEnd);
        PageRequest pageable = new Pagination(from, size, Sort.unsorted());
        List<Event> events = eventRepository.findAllForAdmin(users, states, categories, getRangeStart(rangeStart),
                pageable);
        confirmedRequestForListEvent(events);

        log.info("Get all events in admin {}", events);
        return getEventsFullDtoWithComments(events);
    }

    @Override
    public EventFullDto updateEventByIdAdmin(Long eventId, EventUpdatedDto eventUpdatedDto) {
        Event event = getEventById(eventId);
        updateEventAdmin(event, eventUpdatedDto);
        event = eventRepository.save(event);
        locationRepository.save(event.getLocation());

        Long comments = getComments(eventId);
        log.info("Update event with id= {} in admin ", eventId);
        return mapToEventFullDtoWithComments(event, comments);
    }


    @Override
    public EventFullDto createEventPrivate(Long userId, NewEventDto newEventDto) {
        validateEventDate(newEventDto.getEventDate());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " hasn't found"));
        Category category = getCategoryForEvent(newEventDto.getCategory());
        Location savedLocation = locationRepository
                .save(mapToLocation(newEventDto.getLocation()));
        Event event = eventRepository.save(mapToNewEvent(newEventDto, savedLocation, user, category));
        confirmedRequestsForOneEvent(event);
        log.info("User id= {} create event in admin", userId);
        return mapToEventFullDto(event);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getAllEventsByUserIdPrivate(Long userId, int from, int size) {
        log.info("Get all events of user with id= {} in private", userId);
        List<Event> events = eventRepository.findAllWithInitiatorByInitiatorId(userId, new Pagination(from, size,
                Sort.unsorted()));
        confirmedRequestForListEvent(events);
        return getEventShortDtoWithComments(events);

    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventByIdPrivate(Long userId, Long eventId) {
        Event event = getEventByIdAndInitiatorId(eventId, userId);
        confirmedRequestsForOneEvent(event);
        Long comments = getComments(eventId);
        log.info("Get event with id={} of user with id= {} in private", eventId, userId);
        return mapToEventFullDtoWithComments(event, comments);
    }

    @Override
    public EventFullDto updateEventByIdPrivate(Long userId, Long eventId, EventUpdatedDto eventUpdatedDto) {
        Event event = getEventByIdAndInitiatorId(eventId, userId);
        if (event.getState() == PUBLISHED || event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidateException("Events with CANCELED or PENDING can be updated");
        }
        updateEvent(event, eventUpdatedDto);
        Event eventSaved = eventRepository.save(event);
        locationRepository.save(eventSaved.getLocation());
        Long comments = getComments(eventId);
        log.info("Update event with id={} of user with id= {} in private", eventId, userId);
        return mapToEventFullDtoWithComments(eventSaved, comments);
    }

    public List<EventShortDto> getEventsPublic(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               Boolean onlyAvailable,
                                               EventsSort sort,
                                               Integer from,
                                               Integer size,
                                               HttpServletRequest request) {

        validDateParam(rangeStart, rangeEnd);
        Pagination pageable;
        final EventState state = PUBLISHED;
        List<Event> events;

        if (sort.equals(EVENT_DATE)) {
            pageable = new Pagination(from, size, Sort.by("eventDate"));
        } else {
            pageable = new Pagination(from, size, Sort.unsorted());
        }

        if (onlyAvailable) {
            events = eventRepository.findAllPublishStateNotAvailable(state, getRangeStart(rangeStart), categories,
                    paid, text, pageable);
        } else {
            events = eventRepository.findAllPublishStateAvailable(state, getRangeStart(rangeStart), categories,
                    paid, text, pageable);
        }

        if (rangeEnd != null) {
            events = getEventsBeforeRangeEnd(events, rangeEnd);
        }

        confirmedRequestForListEvent(events);
        List<EventShortDto> result = events.stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList());

        saveViewInEvent(result, rangeStart);
        statsClient.saveStats(app, request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        if (sort.equals(VIEWS)) {
            return result.stream()
                    .sorted(Comparator.comparingLong(EventShortDto::getViews))
                    .collect(Collectors.toList());
        }

        log.info("Get all event with text {}, category {}", text, categories);
        return result;
    }

    @Override
    public EventFullDto getEventByIdPublic(Long id, HttpServletRequest request) {
        Event event = getEventById(id);
        confirmedRequestsForOneEvent(event);
        if (!event.getState().equals(PUBLISHED)) {
            throw new NotFoundException("Event with id=" + id + " hasn't not published");
        }
        Long comments = getComments(id);
        EventFullDto fullDto = mapToEventFullDtoWithComments(event, comments);

        List<String> uris = List.of("/events/" + event.getId());
        List<ViewStatsDto> views = statsClient.getStats(START_DATE, END_DATE, uris, null).getBody();

        if (views != null) {
            fullDto.setViews(views.size());
        }
        statsClient.saveStats(app, request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        log.info("Get event with  id = {}}", id);
        return fullDto;
    }

    private Long getComments(Long eventId) {
        return commentRepository.countCommentByEventId(eventId);
    }

    private List<EventFullDto> getEventsFullDtoWithComments(List<Event> events) {
        Map<Long, Event> eventsMap = events.stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()
                ));
        Set<Long> eventIds = eventsMap.keySet();
        Map<Long, Long> commentCounts = commentRepository.findCommentCountByEventIdList(eventIds).stream()
                .collect(Collectors.toMap(CommentCountDto::getEventId, CommentCountDto::getCount));

        return eventsMap.values().stream()
                .map(event -> {
                    long commentCount = commentCounts.getOrDefault(event.getId(), 0L);
                    return mapToEventFullDtoWithComments(event, commentCount);
                })
                .collect(Collectors.toList());
    }

    private List<EventShortDto> getEventShortDtoWithComments(List<Event> events) {
        Map<Long, Event> eventsMap = events.stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));
        Set<Long> eventIds = eventsMap.keySet();
        Map<Long, Long> commentCounts = commentRepository.findCommentCountByEventIdList(eventIds).stream()
                .collect(Collectors.toMap(CommentCountDto::getEventId, CommentCountDto::getCount));

        return eventsMap.values().stream()
                .map(event -> {
                    long commentCount = commentCounts.getOrDefault(event.getId(), 0L);
                    return mapToEventShortDtoWithComments(event, commentCount);
                })
                .collect(Collectors.toList());
    }

    private Category getCategoryForEvent(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " hasn't found"));
    }

    private Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " hasn't found"));
    }

    private Event getEventByIdAndInitiatorId(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " hasn't found"));
    }

    private void updateEvent(Event event, EventUpdatedDto eventDto) {
        updateEventCommonFields(event, eventDto);

        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(EventStateAction.CANCEL_REVIEW)) {
                event.setState(CANCELED);
            }
            if (eventDto.getStateAction().equals(EventStateAction.SEND_TO_REVIEW)) {
                event.setState(PENDING);
            }
        }
    }

    private void updateEventAdmin(Event event, EventUpdatedDto eventDto) {
        updateEventCommonFields(event, eventDto);

        if (eventDto.getStateAction() != null) {
            if (event.getState().equals(PENDING)) {
                if (eventDto.getStateAction().equals(EventStateAction.REJECT_EVENT)) {
                    event.setState(CANCELED);
                }
                if (eventDto.getStateAction().equals(EventStateAction.PUBLISH_EVENT)) {
                    event.setState(PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
            } else {
                throw new ValidateException("Cannot publish or cancel the event because it's not in the right state: "
                        + event.getState());
            }
        }

        if (eventDto.getEventDate() != null && event.getState().equals(PUBLISHED)) {
            if (eventDto.getEventDate().isAfter(event.getPublishedOn().plusHours(1))) {
                event.setEventDate(eventDto.getEventDate());
            } else {
                throw new ValidateDateException("The event date must be at least 1 hour after the published date.");
            }
        }
    }

    private void updateEventCommonFields(Event event, EventUpdatedDto eventDto) {
        if (eventDto.getAnnotation() != null && !eventDto.getAnnotation().isBlank()) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getDescription() != null && !eventDto.getDescription().isBlank()) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getCategory() != null) {
            Category category = getCategoryForEvent(eventDto.getCategory());
            event.setCategory(category);
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getTitle() != null && !eventDto.getTitle().isBlank()) {
            event.setTitle(eventDto.getTitle());
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(mapToLocation(eventDto.getLocation()));
        }
        if (eventDto.getEventDate() != null) {
            validateEventDate(eventDto.getEventDate());
            event.setEventDate(eventDto.getEventDate());
        }
    }

    private void validDateParam(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new ValidateDateException("The range start date cannot be is after range end date");
            }
        }
    }

    private Long getId(String url) {
        String[] uri = url.split("/");
        return Long.valueOf(uri[uri.length - 1]);
    }

    private void saveViewInEvent(List<EventShortDto> result, LocalDateTime rangeStart) {
        List<String> uris = result.stream()
                .map(eventShortDto -> "/events/" + eventShortDto.getId())
                .collect(Collectors.toList());

        if (rangeStart != null) {
            List<ViewStatsDto> views = statsClient.getStats(
                    rangeStart.format(START_DATE_FORMATTER), LocalDateTime.now().format(START_DATE_FORMATTER),
                    uris, true).getBody();

            if (views != null) {
                Map<Long, Long> mapIdHits = views.stream()
                        .collect(Collectors.toMap(viewStats -> getId(viewStats.getUri()), ViewStatsDto::getHits));

                result.forEach(eventShortDto -> {
                    Long eventId = eventShortDto.getId();
                    Long viewsCount = mapIdHits.getOrDefault(eventId, 0L);
                    eventShortDto.setViews(viewsCount);
                });
            }
        }
    }

    private void validateEventDate(LocalDateTime eventDate) {

        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidateDateException("Event date should be after now");
        }
    }

    private LocalDateTime getRangeStart(LocalDateTime rangeStart) {
        if (rangeStart == null) {
            return LocalDateTime.now();
        }
        return rangeStart;
    }

    private List<Event> getEventsBeforeRangeEnd(List<Event> events, LocalDateTime rangeEnd) {
        return events.stream().filter(event -> event.getEventDate().isBefore(rangeEnd)).collect(Collectors.toList());
    }

    public void confirmedRequestsForOneEvent(Event event) {
        event.setConfirmedRequests(requestRepository
                .countRequestByEventIdAndStatus(event.getId(), EventRequestStatus.CONFIRMED));
    }

    public void confirmedRequestForListEvent(List<Event> events) {
        Map<Event, Long> requestsPerEvent = requestRepository.findAllByEventInAndStatus(events, EventRequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.groupingBy(ParticipationRequest::getEvent, Collectors.counting()));
        if (!requestsPerEvent.isEmpty()) {
            for (Event event : events) {
                event.setConfirmedRequests(requestsPerEvent.getOrDefault(event, 0L));
            }
        }
    }

}