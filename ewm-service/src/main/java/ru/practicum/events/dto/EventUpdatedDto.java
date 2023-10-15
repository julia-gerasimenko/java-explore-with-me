
package ru.practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.locations.dto.LocationDto;
import ru.practicum.util.enam.EventOperationStates;
import javax.validation.constraints.Future;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

import static ru.practicum.util.Constants.DATE_DEFAULT;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdatedDto {

    @Size(min = 5, max = 100)
    private String title;

    @Size(min = 20, max = 5000)
    private String annotation;

    private Long category;

    @Size(min = 100, max = 10000)
    private String description;

    @Future
    @JsonFormat(pattern = DATE_DEFAULT)
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;

    private Boolean requestModeration;

    private EventOperationStates stateAction;
}