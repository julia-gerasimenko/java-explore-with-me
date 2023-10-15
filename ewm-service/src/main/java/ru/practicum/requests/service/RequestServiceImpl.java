package ru.practicum.requests.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.requests.dto.RequestStatusUpdate;
import ru.practicum.requests.dto.StatusUpdateResult;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.handler.ValidationException;
import ru.practicum.handler.NotFoundException;
import ru.practicum.requests.dto.RequestDto;
import ru.practicum.requests.dto.RequestMapper;
import ru.practicum.requests.model.Request;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;
import ru.practicum.util.enam.EventRequestStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static ru.practicum.util.enam.EventStates.PUBLISHED;
import static ru.practicum.util.enam.EventRequestStatus.*;
import static ru.practicum.requests.dto.RequestMapper.mapToNewParticipationRequest;
import static ru.practicum.requests.dto.RequestMapper.mapToParticipationRequestDto;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public RequestDto createRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(()
                -> new NotFoundException("Событие с id = " + eventId + " не было найдено."));
        User user = userRepository.findById(userId).orElseThrow(()
                -> new NotFoundException("Пользователь с id = " + userId + " не был найден."));

        event.setConfirmedRequests(requestRepository
                .countRequestByEventAndStatus(event.getId(), EventRequestStatus.CONFIRMED));
        validateParticipantLimit(event);

        if (userId.equals(event.getInitiator().getId())) {
            throw new ValidationException("Инициатор события не может быть участников своего события");
        }
        if (requestRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new ValidationException("Нельзя создать один и тот же запрос дважды");
        }
        if (!event.getState().equals(PUBLISHED)) {
            throw new ValidationException("Невозможно участвовать в событии, которое не опубликовано.");
        }

        Request request = requestRepository.save(mapToNewParticipationRequest(event, user));

        log.info("Создан запрос на участие {} к событию {} пользователем {}", request, eventId, userId);
        return mapToParticipationRequestDto(request);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RequestDto> getRequestPrivate(Long userId, Long eventId) {

        if (eventRepository.findByIdAndInitiatorId(eventId, userId).isPresent()) {
            return requestRepository.findAllByEvent(eventId).stream()
                    .map(RequestMapper::mapToParticipationRequestDto)
                    .collect(Collectors.toList());
        }
        log.info("Получен запрос на участие в событии с id = {} от пользователя с id = {}", eventId, userId);
        return Collections.emptyList();
    }


    @Override
    public StatusUpdateResult updateEventRequestPrivate(Long userId, Long eventId,
                                                        RequestStatusUpdate statusUpdateRequest) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не был найден.");
        }

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не было найдено."));
        event.setConfirmedRequests(requestRepository
                .countRequestByEventAndStatus(event.getId(), EventRequestStatus.CONFIRMED));

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ValidationException("Невозможно обновить статус если лимит заявок = 0 = 0");
        }
        List<Request> requests = requestRepository.findAllByEventIdAndRequestIds(eventId,
                statusUpdateRequest.getRequestIds());

        validateRequestStatus(requests);

        log.info("Обновлен статус запроса на участие в событии с id= {} пользователя {}", eventId, userId);
        switch (statusUpdateRequest.getStatus()) {
            case CONFIRMED:
                return createConfirmedStatus(requests, event);
            case REJECTED:
                return createStatusRejected(requests, event);
            default:
                throw new ValidationException("состояние не корректно: " + statusUpdateRequest.getStatus());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<RequestDto> getRequestByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не был найден."));
        log.info("Получен запрос на участие от пользователя с id= {}", userId);
        return requestRepository.findAllByUser(userId).stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public RequestDto updateStatusRequest(Long userId, Long requestId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не был найден.");
        }

        Request request = requestRepository.findByIdAndUser(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Запрос с id =" + requestId + " не был найден."));
        request.setStatus(CANCELED);
        log.info("Update status participation request id= {}", requestId);
        return mapToParticipationRequestDto(requestRepository.save(request));
    }

    private StatusUpdateResult createConfirmedStatus(List<Request> requests, Event event) {
        validateParticipantLimit(event);
        long requestingParticipant = event.getParticipantLimit() - event.getConfirmedRequests();

        List<Request> confirmedRequests;
        List<Request> rejectedRequests;

        if (requests.size() <= requestingParticipant) {
            confirmedRequests = requests.stream()
                    .peek(request -> request.setStatus(CONFIRMED))
                    .collect(Collectors.toList());
            rejectedRequests = List.of();
        } else {
            confirmedRequests = requests.stream()
                    .limit(requestingParticipant)
                    .peek(request -> request.setStatus(CONFIRMED))
                    .collect(Collectors.toList());
            rejectedRequests = requests.stream()
                    .skip(requestingParticipant)
                    .peek(request -> request.setStatus(REJECTED))
                    .collect(Collectors.toList());
        }

        event.setConfirmedRequests(event.getConfirmedRequests() + confirmedRequests.size());

        List<Request> updatedRequests = Stream.concat(confirmedRequests.stream(), rejectedRequests.stream())
                .collect(Collectors.toList());
        requestRepository.saveAll(updatedRequests);

        List<RequestDto> confirmedRequestsDto = confirmedRequests.stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());

        List<RequestDto> rejectedRequestsDto = rejectedRequests.stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());

        return new StatusUpdateResult(confirmedRequestsDto, rejectedRequestsDto);
    }

    private StatusUpdateResult createStatusRejected(List<Request> requests, Event event) {
        requests.forEach(request -> request.setStatus(REJECTED));
        requestRepository.saveAll(requests);

        List<RequestDto> rejectedRequests = requests
                .stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
        return new StatusUpdateResult(List.of(), rejectedRequests);
    }

    private void validateParticipantLimit(Event event) {
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= event.getConfirmedRequests()) {
            throw new ValidationException("The event participant number  was reached participation request limit.");
        }
    }

    private void validateRequestStatus(List<Request> requests) {
        boolean isStatusPending = requests.stream()
                .anyMatch(request -> !request.getStatus().equals(PENDING));
        if (isStatusPending) {
            throw new ValidationException("Request status can't be change'");
        }
    }
}