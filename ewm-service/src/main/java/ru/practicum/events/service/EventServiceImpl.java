package ru.practicum.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.events.dto.*;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.Location;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.events.repository.LocationRepository;
import ru.practicum.handler.NotFoundException;
import ru.practicum.handler.ValidateDateException;
import ru.practicum.handler.ValidateException;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;
import ru.practicum.util.Pagination;
import ru.practicum.util.enam.EventState;
import ru.practicum.util.enam.EventStateAction;
import ru.practicum.util.enam.EventsSort;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.events.dto.EventMapper.mapToEventFullDto;
import static ru.practicum.events.dto.EventMapper.mapToNewEvent;
import static ru.practicum.events.dto.LocationMapper.mapToLocation;
import static ru.practicum.util.Constants.*;
import static ru.practicum.util.enam.EventState.*;
import static ru.practicum.util.enam.EventsSort.EVENT_DATE;
import static ru.practicum.util.enam.EventsSort.VIEWS;

@Service
@Slf4j
@RequiredArgsConstructor
@PropertySource(value = {"classpath:application.properties"})
public class EventServiceImpl implements EventService {
    @Value("${app}")
    String app;

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;
    private final LocationRepository locationRepository;


    @Transactional(readOnly = true)
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

        log.info("получены все события, админ {}", events);
        return events.stream()
                .map(EventMapper::mapToEventFullDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventFullDto updateEventByIdAdmin(Long eventId, EventUpdatedDto eventUpdatedDto) {
        Event event = getEventById(eventId);
        updateEventAdmin(event, eventUpdatedDto);
        event = eventRepository.save(event);
        locationRepository.save(event.getLocation());
        log.info("Обновлено событие с id = {}, админ ", eventId);
        return mapToEventFullDto(event);
    }

    @Transactional
    @Override
    public EventFullDto createEventPrivate(Long userId, NewEventDto newEventDto) {
        validateEventDate(newEventDto.getEventDate());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " hasn't found"));
        Category category = getCategoryForEvent(newEventDto.getCategory());
        Location savedLocation = locationRepository
                .save(mapToLocation(newEventDto.getLocation()));
        Event event = eventRepository.save(mapToNewEvent(newEventDto, savedLocation, user, category));
        log.info("Пользователь с id = {} создал событие, админ", userId);
        return mapToEventFullDto(event);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getAllEventsByUserIdPrivate(Long userId, int from, int size) {
        log.info("Получены все события, пользователя с id = {}, приват", userId);
        return eventRepository.findAllWithInitiatorByInitiatorId(userId, new Pagination(from, size,
                        Sort.unsorted())).stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventByIdPrivate(Long userId, Long eventId) {
        Event event = getEventByIdAndInitiatorId(eventId, userId);
        log.info("Получено событие с id = {} пользователя с id = {}, приват", eventId, userId);
        return mapToEventFullDto(event);
    }

    @Transactional
    @Override
    public EventFullDto updateEventByIdPrivate(Long userId, Long eventId, EventUpdatedDto eventUpdatedDto) {
        Event event = getEventByIdAndInitiatorId(eventId, userId);
        if (event.getState() == PUBLISHED || event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidateException("события со статусом CANCELED или PENDING могут быть обновлены");
        }
        updateEvent(event, eventUpdatedDto);
        Event eventSaved = eventRepository.save(event);
        locationRepository.save(eventSaved.getLocation());
        log.info("обновлено событие с id = {} пользователя с id = {}, приват", eventId, userId);
        return mapToEventFullDto(eventSaved);
    }

    @Transactional(readOnly = true)
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

        List<EventShortDto> result = events.stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList());

        saveViewInEvent(result);
        statsClient.saveStats(app, request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        if (sort.equals(VIEWS)) {
            return result.stream()
                    .sorted(Comparator.comparingLong(EventShortDto::getViews))
                    .collect(Collectors.toList());
        }
        log.info("получены все события, содержащие текст {}, категории {}", text, categories);
        return result;
    }

    @Override
    public EventFullDto getEventByIdPublic(Long id, HttpServletRequest request) {
        Event event = getEventById(id);
        if (!event.getState().equals(PUBLISHED)) {
            throw new NotFoundException("событие с id = " + id + " не было опубликовано");
        }
        EventFullDto fullDto = mapToEventFullDto(event);

        List<String> uris = List.of("/events/" + event.getId());
        List<ViewStatsDto> views = statsClient.getStats(START_DATE, END_DATE, uris, null).getBody();

        if (views != null) {
            fullDto.setViews(views.size());
        }
        statsClient.saveStats(app, request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        log.info("получено событие с id = {}}", id);
        return fullDto;
    }


    private Category getCategoryForEvent(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("категория с id = " + id + " не была найдена"));
    }

    private Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("событие с id = " + id + " не было найдено"));
    }

    private Event getEventByIdAndInitiatorId(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("событие с id = " + eventId + " не было найдено"));
    }

    @Transactional
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

    @Transactional
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
                throw new ValidateException("Невозможно отменить или опубликовать событие в связи с его статусом: "
                        + event.getState());
            }
        }

        if (eventDto.getEventDate() != null && event.getState().equals(PUBLISHED)) {
            if (eventDto.getEventDate().isAfter(event.getPublishedOn().plusHours(1))) {
                event.setEventDate(eventDto.getEventDate());
            } else {
                throw new ValidateDateException("Событие должно длиться как минимум 1 час.");
            }
        }
    }

    @Transactional
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
                throw new ValidateDateException("Дата старта не может быть позже даты окончания");
            }
        }
    }

    private Long getId(String url) {
        String[] uri = url.split("/");
        return Long.valueOf(uri[uri.length - 1]);
    }

    private void saveViewInEvent(List<EventShortDto> result) {
        List<String> uris = result.stream()
                .map(eventShortDto -> "/events/" + eventShortDto.getId())
                .collect(Collectors.toList());

        List<Long> eventIds = result.stream()
                .map(EventShortDto::getId)
                .collect(Collectors.toList());

        Optional<LocalDateTime> start = eventRepository.getStart(eventIds);

        if (start.isPresent()) {
            List<ViewStatsDto> views = statsClient.getStats(
                    start.get().format(DateTimeFormatter.ofPattern(DATE_DEFAULT)), LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern(DATE_DEFAULT)),
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
            throw new ValidateDateException("Дата события должна быть позже текущего времени");
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
}