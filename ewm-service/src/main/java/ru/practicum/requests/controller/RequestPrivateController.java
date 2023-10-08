package ru.practicum.requests.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class RequestPrivateController {

    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable(value = "userId") Long userId,
                                          @RequestParam(value = "eventId") Long eventId) {
        return requestService.createParticipationRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto updateParticipationRequestStatusToCancel(
            @PathVariable(value = "userId") Long userId,
            @PathVariable(value = "requestId") Long requestId) {
        return requestService.updateStatusParticipationRequest(userId, requestId);
    }

    @GetMapping
    public List<ParticipationRequestDto> getParticipationRequest(@PathVariable(value = "userId") Long userId) {
        return requestService.getParticipationRequestByUserId(userId);
    }
}