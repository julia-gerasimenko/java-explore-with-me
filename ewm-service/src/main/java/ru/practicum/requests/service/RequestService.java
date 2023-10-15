package ru.practicum.requests.service;

import ru.practicum.requests.dto.RequestStatusUpdate;
import ru.practicum.requests.dto.StatusUpdateResult;
import ru.practicum.requests.dto.RequestDto;

import java.util.List;

public interface RequestService {
    RequestDto createRequest(Long userId, Long eventId);

    List<RequestDto> getRequestByUserId(Long userId);

    RequestDto updateStatusRequest(Long userId, Long requestId);

    List<RequestDto> getRequestPrivate(Long userId, Long eventId);

    StatusUpdateResult updateEventRequestPrivate(Long userId, Long eventId,
                                                 RequestStatusUpdate dtoRequest);

}