package ru.practicum.requests.dto;

import lombok.*;
import ru.practicum.events.dto.EventUpdatedDto;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class StatusUpdateResult extends EventUpdatedDto {

    private List<RequestDto> confirmedRequests;

    private List<RequestDto> rejectedRequests;
}