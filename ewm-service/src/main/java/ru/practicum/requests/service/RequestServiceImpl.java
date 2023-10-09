package ru.practicum.requests.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.handler.NotFoundException;
import ru.practicum.handler.ValidateException;
import ru.practicum.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.dto.ParticipationRequestMapper;
import ru.practicum.requests.model.ParticipationRequest;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.practicum.requests.dto.ParticipationRequestMapper.mapToNewParticipationRequest;
import static ru.practicum.requests.dto.ParticipationRequestMapper.mapToParticipationRequestDto;
import static ru.practicum.util.enam.EventRequestStatus.*;
import static ru.practicum.util.enam.EventState.PUBLISHED;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private EventRequestStatusUpdateResult createConfirmedStatus(List<ParticipationRequest> requests, Event event) {
        validateParticipantLimit(event);
        int potentialParticipant = event.getParticipantLimit() - event.getConfirmedRequests();

        List<ParticipationRequest> confirmedRequests;
        List<ParticipationRequest> rejectedRequests;

        if (requests.size() <= potentialParticipant) {
            confirmedRequests = requests.stream()
                    .peek(request -> request.setStatus(CONFIRMED))
                    .collect(Collectors.toList());
            rejectedRequests = List.of();
        } else {
            confirmedRequests = requests.stream()
                    .limit(potentialParticipant)
                    .peek(request -> request.setStatus(CONFIRMED))
                    .collect(Collectors.toList());
            rejectedRequests = requests.stream()
                    .skip(potentialParticipant)
                    .peek(request -> request.setStatus(REJECTED))
                    .collect(Collectors.toList());
        }

        event.setConfirmedRequests(event.getConfirmedRequests() + confirmedRequests.size());
        eventRepository.save(event);

        List<ParticipationRequest> updatedRequests = Stream.concat(confirmedRequests.stream(), rejectedRequests.stream())
                .collect(Collectors.toList());
        requestRepository.saveAll(updatedRequests);

        List<ParticipationRequestDto> confirmedRequestsDto = confirmedRequests.stream()
                .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());

        List<ParticipationRequestDto> rejectedRequestsDto = rejectedRequests.stream()
                .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());

        return new EventRequestStatusUpdateResult(confirmedRequestsDto, rejectedRequestsDto);
    }


    @Override
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new NotFoundException("Пользователь с id = " + userId + " не был найден."));
        Event event = eventRepository.findById(eventId).orElseThrow(()
                -> new NotFoundException("Событие с id = " + eventId + "  не было найдено."));

        validateParticipantLimit(event);

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ValidateException("Нельзя сделать один и тот же запрос дважды");
        }

        if (userId.equals(event.getInitiator().getId())) {
            throw new ValidateException("Инициатор события не может быть его участником");
        }

        if (!event.getState().equals(PUBLISHED)) {
            throw new ValidateException("Невозможно принять участие в неопубликованном событии");
        }

        ParticipationRequest participationRequest = requestRepository.save(mapToNewParticipationRequest(event, user));

        if (participationRequest.getStatus() == CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        log.info("Создан запрос на участие {} ", participationRequest);
        return mapToParticipationRequestDto(participationRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getParticipationRequestPrivate(Long userId, Long eventId) {

        if (eventRepository.findByIdAndInitiatorId(eventId, userId).isPresent()) {
            return requestRepository.findAllByEventId(eventId).stream()
                    .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                    .collect(Collectors.toList());
        }
        log.info("Получен зарпос на участие в событии с id = {} пользователя с id = {}", eventId, userId);
        return Collections.emptyList();
    }

    private EventRequestStatusUpdateResult createRejectedStatus(List<ParticipationRequest> requests, Event event) {
        requests.forEach(request -> request.setStatus(REJECTED));
        requestRepository.saveAll(requests);
        List<ParticipationRequestDto> rejectedRequests = requests
                .stream()
                .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
        return new EventRequestStatusUpdateResult(List.of(), rejectedRequests);
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequestStatusPrivate(Long userId, Long eventId,
                                                                          EventRequestStatusUpdateRequest statusUpdateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не было найдено."));

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ValidateException("Невозможно обновить статус когда лимит заявок = 0");
        }
        List<ParticipationRequest> requests = requestRepository.findAllByEventIdAndIdIn(eventId,
                statusUpdateRequest.getRequestIds());

        validateRequestStatus(requests);

        log.info("Обновления запроса к событию с id = {}, пользователя с id = {}", eventId, userId);
        switch (statusUpdateRequest.getStatus()) {
            case CONFIRMED:
                return createConfirmedStatus(requests, event);
            case REJECTED:
                return createRejectedStatus(requests, event);
            default:
                throw new ValidateException("Статус не корректный: " + statusUpdateRequest.getStatus());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getParticipationRequestByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь id=" + userId + " не был найден"));
        log.info("Получен запрос на участие от пользователя с id = {}", userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto updateStatusParticipationRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Запрос с id = " + requestId + " не ыбл найден"));
        request.setStatus(CANCELED);
        log.info("Обновлен статус на запроса на участие с id = {}, id пользователя = {}", requestId, userId);
        return mapToParticipationRequestDto(requestRepository.save(request));
    }

    private void validateRequestStatus(List<ParticipationRequest> requests) {
        boolean isStatusPending = requests.stream()
                .anyMatch(request -> !request.getStatus().equals(PENDING));
        if (isStatusPending) {
            throw new ValidateException("Статус запроса нельзя изменить");
        }
    }

    private void validateParticipantLimit(Event event) {
        if (event.getParticipantLimit() > 0 && event.getConfirmedRequests().equals(event.getParticipantLimit())) {
            log.debug("Количество участников события {} достигло лимита", event);
            throw new ValidateException("Количество участников события достигло лимита");
        }
    }
}