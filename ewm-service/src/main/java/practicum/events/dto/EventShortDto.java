package practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import practicum.category.dto.CategoryDto;
import practicum.users.dto.UserShortDto;

import java.time.LocalDateTime;

import static practicum.util.Constants.DATE_DEFAULT;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EventShortDto {

    private String title;

    private String annotation;

    private CategoryDto category;

    private Integer confirmedRequests;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_DEFAULT)
    private LocalDateTime eventDate;

    private Long id;

    private UserShortDto initiator;

    private Boolean paid;

    private Long views;
}