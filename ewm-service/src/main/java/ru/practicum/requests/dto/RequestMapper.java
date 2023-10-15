package ru.practicum.requests.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.events.model.Event;
import ru.practicum.requests.model.Request;
import ru.practicum.users.model.User;

import static ru.practicum.util.enam.EventRequestStatus.CONFIRMED;


@UtilityClass
public class RequestMapper {

    public static RequestDto mapToParticipationRequestDto(Request request) {
        return RequestDto.builder()
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .id(request.getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }

    public static Request mapToNewParticipationRequest(Event event, User user) {
        Request request = new Request();
        request.setEvent(event);
        request.setRequester(user);

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(CONFIRMED);
        }
        return request;
    }
}
