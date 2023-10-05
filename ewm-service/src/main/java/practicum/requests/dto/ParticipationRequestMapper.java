package practicum.requests.dto;

import lombok.experimental.UtilityClass;
import practicum.events.model.Event;
import practicum.requests.model.ParticipationRequest;
import practicum.users.model.User;

import static practicum.util.enam.EventRequestStatus.CONFIRMED;


@UtilityClass
public class ParticipationRequestMapper {

    public static ParticipationRequestDto mapToParticipationRequestDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .id(request.getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }

    public static ParticipationRequest mapToNewParticipationRequest(Event event, User user) {
        ParticipationRequest request = new ParticipationRequest();
        request.setEvent(event);
        request.setRequester(user);

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(CONFIRMED);
        }
        return request;
    }

}
