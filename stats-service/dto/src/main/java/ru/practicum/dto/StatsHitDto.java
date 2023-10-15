package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static ru.practicum.dto.Constants.DATE_TIME_PATTERN;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsHitDto {

    @NotBlank(message = "Поле App не может быть пустым")
    private String app;

    @NotBlank(message = "Поле URI не может быть пустым")
    private String uri;

    @NotBlank(message = "Поле IP не может быть пустым")
    private String ip;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
    @NotNull
    private LocalDateTime timestamp;
}