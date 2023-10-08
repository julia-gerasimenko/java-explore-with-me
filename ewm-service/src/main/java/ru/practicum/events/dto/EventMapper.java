package ru.practicum.events.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.category.model.Category;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.Location;
import ru.practicum.users.model.User;

import static ru.practicum.category.dto.CategoryMapper.toCategoryDto;
import static ru.practicum.events.dto.LocationMapper.mapToLocationDto;
import static ru.practicum.users.dto.UserMapper.toUserShortDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    public static EventFullDto mapToEventFullDto(Event event) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .location(mapToLocationDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .title(event.getTitle())
                .state(event.getState())
                .description(event.getDescription())
                .category(toCategoryDto(event.getCategory()))
                .createdOn(event.getCreatedOn())
                .initiator(toUserShortDto(event.getInitiator()))
                .requestModeration(event.getRequestModeration())
                .publishedOn(event.getPublishedOn())
                .build();
    }

    public static EventShortDto mapToEventShortDto(Event event) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .category(toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .build();
    }

    public static Event mapToNewEvent(NewEventDto eventDto, Location location, User user, Category category) {
        Event event = new Event();
        event.setAnnotation(eventDto.getAnnotation());
        event.setCategory(category);
        event.setDescription(eventDto.getDescription());
        event.setEventDate(eventDto.getEventDate());
        event.setLocation(location);
        event.setPaid(eventDto.getPaid());
        event.setParticipantLimit(eventDto.getParticipantLimit());
        event.setRequestModeration(eventDto.getRequestModeration());
        event.setTitle(eventDto.getTitle());
        event.setInitiator(user);
        return event;
    }

}