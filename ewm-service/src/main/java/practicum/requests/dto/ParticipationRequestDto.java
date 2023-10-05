package practicum.requests.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import practicum.util.enam.EventRequestStatus;

import java.time.LocalDateTime;

import static practicum.util.Constants.DATE_DEFAULT;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ParticipationRequestDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_DEFAULT)
    private LocalDateTime created;

    private Long event;

    private Long id;

    private Long requester;

    private EventRequestStatus status;

}