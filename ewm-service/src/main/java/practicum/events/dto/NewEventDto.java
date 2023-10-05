package practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

import static practicum.util.Constants.DATE_DEFAULT;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewEventDto {

    @Size(min = 3, max = 120)
    @NotBlank
    private String title;

    @Size(min = 20, max = 2000)
    @NotBlank
    private String annotation;

    @NotNull(message = "Category can't be blank")
    private Long category;

    @Size(min = 20, max = 7000)
    @NotBlank
    private String description;

    @NotNull
    @JsonFormat(pattern = DATE_DEFAULT)
    @Future
    private LocalDateTime eventDate;

    @NotNull
    @Valid
    private LocationDto location;

    @NotNull
    private Boolean paid = false;

    @PositiveOrZero
    private int participantLimit = 0;

    @NotNull
    private Boolean requestModeration = true;
}