package ru.practicum.requests.dto;

import lombok.*;
import ru.practicum.events.dto.EventUpdatedDto;

import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class EventRequestStatusUpdateRequest extends EventUpdatedDto {

    private Set<Long> requestIds;

    private EventRequestStatus status;

    public enum EventRequestStatus {
        CONFIRMED,
        REJECTED
    }
}