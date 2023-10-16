package ru.practicum.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.comments.dto.CommentAmountDto;
import ru.practicum.comments.repository.CommentsRepository;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.locations.model.Location;
import ru.practicum.locations.repository.LocationsRepository;
import ru.practicum.requests.model.Request;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.util.enam.EventRequestStatus;
import ru.practicum.util.enam.EventStates;
import ru.practicum.util.enam.EventsSorting;
import ru.practicum.util.Pagination;
import ru.practicum.StatsClient;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.util.enam.EventOperationStates;
import ru.practicum.events.dto.*;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.handler.NotFoundException;
import ru.practicum.handler.ValidationException;
import ru.practicum.handler.ValidationDateException;
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
import static ru.practicum.util.enam.EventsSorting.EVENT_DATE;
import static ru.practicum.util.enam.EventsSorting.VIEWS;
import static ru.practicum.util.Constants.*;
import static ru.practicum.util.enam.EventStates.*;

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
    private final StatsClient statsClient;
    private final LocationsRepository locationsRepository;
    private final CommentsRepository commentsRepository;
    private final RequestRepository requestRepository;

    @Override
    public List<EventFullDto> getAllEventsAdmin(List<Long> users,
                                                List<EventStates> states,
                                                List<Long> categories,
                                                LocalDateTime rangeStart,
                                                LocalDateTime rangeEnd,
                                                Integer from,
                                                Integer size) {

        validDateParameters(rangeStart, rangeEnd);
        PageRequest pageable = new Pagination(from, size, Sort.unsorted());
        List<Event> events = eventRepository.findAllForAdmin(users, states, categories, getRangeStart(rangeStart),
                pageable);
        setConfirmedRequestForEventList(events);

        log.info("Получен список всех событий, admin {}", events);
        return getEventsFullDtoWithComments(events);
    }

    @Override
    public EventFullDto updateEventByIdAdmin(Long eventId, EventUpdatedDto eventUpdatedDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " hasn't found"));

        updateEventAdmin(event, eventUpdatedDto);
        event = eventRepository.save(event);
        locationsRepository.save(event.getLocation());

        Long comments = getComments(eventId);
        log.info("Обновлено событие с id = {}, admin ", eventId);
        return mapToEventFullDtoWithComments(event, comments);
    }


    @Override
    public EventFullDto createEventPrivate(Long userId, NewEventDto newEventDto) {
        validateDateOfEvent(newEventDto.getEventDate());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не был найден."));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + newEventDto.getCategory() + " hasn't found"));

        Location savedLocation = locationsRepository
                .save(mapToLocation(newEventDto.getLocation()));
        Event event = eventRepository.save(mapToNewEvent(newEventDto, savedLocation, user, category));

        setConfirmedRequestsForEvent(event);
        log.info("Пользователь с id = {} создал событие {}, admin", userId, event);
        return mapToEventFullDto(event);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getAllEventsByUserIdPrivate(Long userId, int from, int size) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не был найден.");
        }

        List<Event> events = eventRepository.findAllWithInitiatorByInitiatorId(userId, new Pagination(from, size,
                Sort.unsorted()));
        log.info("Получен список всех событий с id = {} from {}, size {}, private", userId, from, size);
        setConfirmedRequestForEventList(events);
        return getEventShortDtoWithComments(events);

    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventByIdPrivate(Long userId, Long eventId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не был найден.");
        }
        if (eventRepository.findById(eventId).isEmpty()) {
            throw new NotFoundException("Событие с id = " + userId + " не было найдено.");
        }

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не было найдено."));
        setConfirmedRequestsForEvent(event);
        Long comments = getComments(eventId);
        log.info("Получено событие с id = {} пользователя с id = {}, private", eventId, userId);
        return mapToEventFullDtoWithComments(event, comments);
    }

    @Override
    public EventFullDto updateEventByIdPrivate(Long userId, Long eventId, EventUpdatedDto eventUpdatedDto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не было найдено."));

        if (event.getState() == PUBLISHED || event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("События со статусом CANCELED или PENDING можно обновить");
        }

        updateEvent(event, eventUpdatedDto);
        Event eventSaved = eventRepository.save(event);
        locationsRepository.save(eventSaved.getLocation());
        Long comments = getComments(eventId);
        log.info("Обновлено событие с id = {} пользователя с id = {}, private", eventId, userId);
        return mapToEventFullDtoWithComments(eventSaved, comments);
    }

    public List<EventShortDto> getEventsPublic(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               Boolean onlyAvailable,
                                               EventsSorting sort,
                                               Integer from,
                                               Integer size,
                                               HttpServletRequest request) {

        validDateParameters(rangeStart, rangeEnd);
        Pagination pageable;
        final EventStates state = PUBLISHED;
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
            events = getEventsBeforeTheEnd(events, rangeEnd);
        }

        setConfirmedRequestForEventList(events);
        List<EventShortDto> result = events.stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList());

        saveEventView(result, rangeStart);
        statsClient.saveStats(app, request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        if (sort.equals(VIEWS)) {
            return result.stream()
                    .sorted(Comparator.comparingLong(EventShortDto::getViews))
                    .collect(Collectors.toList());
        }

        log.info("Получены все события, включающие в описании текст {}, категории {}", text, categories);
        return result;
    }

    @Override
    public EventFullDto getEventByIdPublic(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " hasn't found"));

        setConfirmedRequestsForEvent(event);
        if (!event.getState().equals(PUBLISHED)) {
            throw new NotFoundException("Event with id=" + eventId + " hasn't not published");
        }
        Long comments = getComments(eventId);
        EventFullDto fullDto = mapToEventFullDtoWithComments(event, comments);

        List<String> uris = List.of("/events/" + event.getId());
        List<ViewStatsDto> views = statsClient.getStats(START_DATE, END_DATE, uris, null).getBody();

        if (views != null) {
            fullDto.setViews(views.size());
        }
        statsClient.saveStats(app, request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        log.info("Получено событие с id = {}}", eventId);
        return fullDto;
    }

    private Long getComments(Long eventId) {
        return commentsRepository.countCommentByEventId(eventId);
    }

    private List<EventFullDto> getEventsFullDtoWithComments(List<Event> events) {
        Map<Long, Event> eventsMap = events.stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()
                ));
        Set<Long> eventIds = eventsMap.keySet();
        Map<Long, Long> commentCounts = commentsRepository.findCommentCountByEventIdList(eventIds).stream()
                .collect(Collectors.toMap(CommentAmountDto::getEventId, CommentAmountDto::getCount));

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
        Map<Long, Long> commentCounts = commentsRepository.findCommentCountByEventIdList(eventIds).stream()
                .collect(Collectors.toMap(CommentAmountDto::getEventId, CommentAmountDto::getCount));

        return eventsMap.values().stream()
                .map(event -> {
                    long commentCount = commentCounts.getOrDefault(event.getId(), 0L);
                    return mapToEventShortDtoWithComments(event, commentCount);
                })
                .collect(Collectors.toList());
    }

    private void updateEvent(Event event, EventUpdatedDto eventDto) {
        updateEventFields(event, eventDto);

        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(EventOperationStates.CANCEL_REVIEW)) {
                event.setState(CANCELED);
            }
            if (eventDto.getStateAction().equals(EventOperationStates.SEND_TO_REVIEW)) {
                event.setState(PENDING);
            }
        }
    }

    private void updateEventAdmin(Event event, EventUpdatedDto eventDto) {
        updateEventFields(event, eventDto);

        if (eventDto.getStateAction() != null) {
            if (event.getState().equals(PENDING)) {
                if (eventDto.getStateAction().equals(EventOperationStates.REJECT_EVENT)) {
                    event.setState(CANCELED);
                }
                if (eventDto.getStateAction().equals(EventOperationStates.PUBLISH_EVENT)) {
                    event.setState(PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
            } else {
                throw new ValidationException("Невозможно опубликовать или сохранить событие в связи с его состоянием: "
                        + event.getState());
            }
        }

        if (eventDto.getEventDate() != null && event.getState().equals(PUBLISHED)) {
            if (eventDto.getEventDate().isAfter(event.getPublishedOn().plusHours(1))) {
                event.setEventDate(eventDto.getEventDate());
            } else {
                throw new ValidationDateException("Дата события должна быть " +
                        "позже как минимум на 1 час от даты его публикации.");
            }
        }
    }

    private void updateEventFields(Event event, EventUpdatedDto eventDto) {
        if (eventDto.getCategory() != null) {
            Category category = categoryRepository.findById(eventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + eventDto.getCategory() + " hasn't found"));
            event.setCategory(category);
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getAnnotation() != null && !eventDto.getAnnotation().isBlank()) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getTitle() != null && !eventDto.getTitle().isBlank()) {
            event.setTitle(eventDto.getTitle());
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(mapToLocation(eventDto.getLocation()));
        }
        if (eventDto.getDescription() != null && !eventDto.getDescription().isBlank()) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getEventDate() != null) {
            validateDateOfEvent(eventDto.getEventDate());
            event.setEventDate(eventDto.getEventDate());
        }
    }

    private Long getId(String url) {
        String[] uri = url.split("/");
        return Long.valueOf(uri[uri.length - 1]);
    }

    private void validDateParameters(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new ValidationDateException("Дата старта не может быть позже даты начала");
            }
        }
    }

    private void saveEventView(List<EventShortDto> result, LocalDateTime rangeStart) {
        List<String> uris = result.stream()
                .map(eventShortDto -> "/events/" + eventShortDto.getId())
                .collect(Collectors.toList());

        if (rangeStart != null) {
            List<ViewStatsDto> views = statsClient.getStats(
                    rangeStart.format(START_DATE_FORMATTER), LocalDateTime.now().format(START_DATE_FORMATTER),
                    uris, true).getBody();

            if (views != null) {
                Map<Long, Long> map = views.stream()
                        .collect(Collectors.toMap(viewStats -> getId(viewStats.getUri()), ViewStatsDto::getHits));

                result.forEach(eventShortDto -> {
                    Long eventId = eventShortDto.getId();
                    Long viewsCount = map.getOrDefault(eventId, 0L);
                    eventShortDto.setViews(viewsCount);
                });
            }
        }
    }

    private LocalDateTime getRangeStart(LocalDateTime rangeStart) {
        if (rangeStart == null) {
            return LocalDateTime.now();
        }
        return rangeStart;
    }

    private List<Event> getEventsBeforeTheEnd(List<Event> events, LocalDateTime rangeEnd) {
        return events.stream().filter(event -> event.getEventDate().isBefore(rangeEnd)).collect(Collectors.toList());
    }

    private void validateDateOfEvent(LocalDateTime eventDate) {

        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationDateException("Дата начала события должна быть позже текущего времени.");
        }
    }

    public void setConfirmedRequestForEventList(List<Event> events) {
        Map<Event, Long> requestsPerEvent = requestRepository.findAllByEventInAndStatus(events, EventRequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.groupingBy(Request::getEvent, Collectors.counting()));
        if (!requestsPerEvent.isEmpty()) {
            for (Event event : events) {
                event.setConfirmedRequests(requestsPerEvent.getOrDefault(event, 0L));
            }
        }
    }

    public void setConfirmedRequestsForEvent(Event event) {
        event.setConfirmedRequests(requestRepository
                .countRequestByEventIdAndStatus(event.getId(), EventRequestStatus.CONFIRMED));
    }

}