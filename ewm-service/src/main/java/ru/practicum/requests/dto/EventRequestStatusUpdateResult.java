package ru.practicum.requests.dto;

import lombok.*;
import ru.practicum.events.dto.EventUpdatedDto;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class EventRequestStatusUpdateResult extends EventUpdatedDto {

    private List<ParticipationRequestDto> confirmedRequests;

    private List<ParticipationRequestDto> rejectedRequests;
}