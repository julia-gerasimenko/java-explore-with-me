package ru.practicum.requests.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestPrivateController {

    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable(value = "userId") Long userId,
                                          @RequestParam(value = "eventId") Long eventId) {
        log.info("Create participation request of event id= {} for user with id= {} ", eventId, userId);
        return requestService.createParticipationRequest(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> getParticipationRequest(@PathVariable(value = "userId") Long userId) {
        log.info("Create participation requests for with id= {} ", userId);
        return requestService.getParticipationRequestByUserId(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto updateParticipationRequestStatusToCancel(@PathVariable(value = "userId") Long userId,
                                                                            @PathVariable(value = "requestId") Long requestId) {
        log.info("Update participation request of event id= {} for user with id= {} ", requestId, userId);
        return requestService.updateStatusParticipationRequest(userId, requestId);
    }
}