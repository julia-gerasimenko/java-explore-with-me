package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static ru.practicum.dto.Constant.DATE_TIME_PATTERN;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsHitDto {

    @NotBlank(message = "App can't be blank")
    private String app;

    @NotBlank(message = "URI can't be blank")
    private String uri;

    @NotBlank(message = "IP can't be blank")
    private String ip;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
    @NotNull
    private LocalDateTime timestamp;
}