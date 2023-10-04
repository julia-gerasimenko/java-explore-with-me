package ru.practicum.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.dto.StatsHitDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class StatsHitMapperTest {
    private StatsHitDto statsHitDto;

    @BeforeEach
    void beforeEach() {
        LocalDateTime time = LocalDateTime.of(2022, 3, 5, 23, 0);
        statsHitDto = StatsHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/2")
                .ip("192.163.0.1")
                .timestamp(time)
                .build();
    }

    @Test
    void statsHitDtoToStatHitTest() {
        StatHit statHit = StatsHitMapper.statsHitDtoToStatHit(statsHitDto);

        assertThat(statHit.getApp()).isEqualTo(statsHitDto.getApp());
        assertThat(statHit.getIp()).isEqualTo(statsHitDto.getIp());
        assertThat(statHit.getUri()).isEqualTo(statsHitDto.getUri());
        assertThat(statHit.getTimestamp()).isEqualTo(statsHitDto.getTimestamp());
    }
}