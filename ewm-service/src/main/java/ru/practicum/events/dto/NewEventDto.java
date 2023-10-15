package ru.practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.practicum.locations.dto.LocationDto;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

import static ru.practicum.util.Constants.DATE_DEFAULT;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewEventDto {

    @NotBlank
    @Size(min = 5, max = 100)
    private String title;

    @NotBlank
    @Size(min = 20, max = 5000)
    private String annotation;

    @NotNull(message = "Категория е может быть пустой или отсутствовать")
    private Long category;

    @NotBlank
    @Size(min = 100, max = 10000)
    private String description;

    @NotNull
    @Future
    @JsonFormat(pattern = DATE_DEFAULT)
    private LocalDateTime eventDate;

    @Valid
    @NotNull
    private LocationDto location;

    @NotNull
    private Boolean paid = false;

    @PositiveOrZero
    private int participantLimit = 0;

    @NotNull
    private Boolean requestModeration = true;
}