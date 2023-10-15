package ru.practicum.comments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.Null;
import java.time.LocalDateTime;

import static ru.practicum.util.Constants.DATE_DEFAULT;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CommentDto {
    @Null
    private Long id;

    private String text;

    @Null
    private Long eventId;

    @Null
    private Long authorId;

    @Null
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_DEFAULT)
    private LocalDateTime created;
}
