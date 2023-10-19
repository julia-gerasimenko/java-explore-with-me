package ru.practicum.requests.service;

import ru.practicum.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.requests.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto createParticipationRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getParticipationRequestByUserId(Long userId);

    ParticipationRequestDto updateStatusParticipationRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getParticipationRequestPrivate(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequestStatusPrivate(Long userId, Long eventId,
                                                                   EventRequestStatusUpdateRequest dtoRequest);

}