package ru.practicum.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonTest
class StatHitTest {
    @Autowired
    private JacksonTester<StatHit> json;

    @Test
    void itemRequestDtoTest() throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime time = LocalDateTime.of(2022, 3, 5, 23, 0);
        StatHit statHit = StatHit.builder()
                .id(1)
                .app("ewm-main-service")
                .uri("/events/2")
                .ip("192.163.0.1")
                .timestamp(LocalDateTime.parse(time.format(formatter)))
                .build();
        JsonContent<StatHit> result = json.write(statHit);

        Assertions.assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(result).extractingJsonPathStringValue("$.app").isEqualTo(statHit.getApp());
        Assertions.assertThat(result).extractingJsonPathStringValue("$.uri").isEqualTo(statHit.getUri());
        Assertions.assertThat(result).extractingJsonPathStringValue("$.ip").isEqualTo(statHit.getIp());
        Assertions.assertThat(result).extractingJsonPathStringValue("$.timestamp").isEqualTo(time.format(formatter));
    }
}