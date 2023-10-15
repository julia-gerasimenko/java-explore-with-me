package ru.practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.locations.dto.LocationDto;
import ru.practicum.util.enam.EventStates;
import ru.practicum.users.dto.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.util.Constants.DATE_DEFAULT;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EventFullDto {

    private Long id;

    private String title;

    private String description;

    private String annotation;

    private CategoryDto category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_DEFAULT)
    private LocalDateTime eventDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_DEFAULT)
    private LocalDateTime publishedOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_DEFAULT)
    private LocalDateTime createdOn;

    private LocationDto location;

    private boolean paid;

    private int participantLimit;

    private long confirmedRequests;

    private long views;

    private EventStates state;

    private boolean requestModeration;

    private UserShortDto initiator;

    private long comments;
}