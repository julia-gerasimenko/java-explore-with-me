package practicum.requests.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practicum.events.model.Event;
import practicum.events.repository.EventRepository;
import practicum.handler.NotFoundException;
import practicum.handler.ValidateException;
import practicum.requests.dto.EventRequestStatusUpdateRequest;
import practicum.requests.dto.EventRequestStatusUpdateResult;
import practicum.requests.dto.ParticipationRequestDto;
import practicum.requests.dto.ParticipationRequestMapper;
import practicum.requests.model.ParticipationRequest;
import practicum.requests.repository.RequestRepository;
import practicum.users.model.User;
import practicum.users.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static practicum.requests.dto.ParticipationRequestMapper.mapToNewParticipationRequest;
import static practicum.requests.dto.ParticipationRequestMapper.mapToParticipationRequestDto;
import static practicum.util.enam.EventRequestStatus.*;
import static practicum.util.enam.EventState.PUBLISHED;

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
                -> new NotFoundException("User with id=" + userId + " hasn't found."));
        Event event = eventRepository.findById(eventId).orElseThrow(()
                -> new NotFoundException("Event with id=" + eventId + "  hasn't found found."));

        validateParticipantLimit(event);

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ValidateException("You can't create same request twice");
        }

        if (userId.equals(event.getInitiator().getId())) {
            throw new ValidateException("Initiator of the event can't participate in own event");
        }

        if (!event.getState().equals(PUBLISHED)) {
            throw new ValidateException("It isn't possible participate if event isn't published ");
        }

        ParticipationRequest participationRequest = requestRepository.save(mapToNewParticipationRequest(event, user));

        if (participationRequest.getStatus() == CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        log.info("Create participation request {} ", participationRequest);
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
        log.info("Get participation request for event with id ={}", eventId);
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
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " hasn't found."));

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ValidateException("It isn't possible to update status if the application limit = 0");
        }
        List<ParticipationRequest> requests = requestRepository.findAllByEventIdAndIdIn(eventId,
                statusUpdateRequest.getRequestIds());

        validateRequestStatus(requests);

        log.info("Update event request status id= {}", eventId);
        switch (statusUpdateRequest.getStatus()) {
            case CONFIRMED:
                return createConfirmedStatus(requests, event);
            case REJECTED:
                return createRejectedStatus(requests, event);
            default:
                throw new ValidateException("State is not valid: " + statusUpdateRequest.getStatus());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getParticipationRequestByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " hasn't found"));
        log.info("Get participation request for user with id= {}", userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto updateStatusParticipationRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " hasn't found"));
        request.setStatus(CANCELED);
        log.info("Update status participation request id= {}", requestId);
        return mapToParticipationRequestDto(requestRepository.save(request));
    }

    private void validateRequestStatus(List<ParticipationRequest> requests) {
        boolean isStatusPending = requests.stream()
                .anyMatch(request -> !request.getStatus().equals(PENDING));
        if (isStatusPending) {
            throw new ValidateException("Request status can't be change'");
        }
    }

    private void validateParticipantLimit(Event event) {
        if (event.getParticipantLimit() > 0 && event.getConfirmedRequests().equals(event.getParticipantLimit())) {
            throw new ValidateException("The event participant number  was reached participation request limit.");
        }
    }
}