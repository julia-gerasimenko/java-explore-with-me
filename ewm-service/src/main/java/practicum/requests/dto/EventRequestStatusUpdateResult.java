package practicum.requests.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import practicum.events.dto.EventUpdatedDto;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class EventRequestStatusUpdateResult extends EventUpdatedDto {

    private List<ParticipationRequestDto> confirmedRequests;

    private List<ParticipationRequestDto> rejectedRequests;
}