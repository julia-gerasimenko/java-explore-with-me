package ru.practicum.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.StatsHitDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatsHitMapper {

    public static StatHit statsHitDtoToStatHit(StatsHitDto statsHitDto) {
        return StatHit.builder()
                .app(statsHitDto.getApp())
                .uri(statsHitDto.getUri())
                .ip(statsHitDto.getIp())
                .timestamp(statsHitDto.getTimestamp())
                .build();
    }
}