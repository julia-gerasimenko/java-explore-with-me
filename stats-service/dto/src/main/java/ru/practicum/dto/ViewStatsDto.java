package ru.practicum.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ViewStatsDto {

    private String app;

    private String uri;

    private long hits;
}