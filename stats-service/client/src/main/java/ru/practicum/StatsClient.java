package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.dto.StatsHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@PropertySource(value = {"classpath:statsServiceClient.properties"})
public class StatsClient {

    @Value("${stats.server.url}")
    private String baseUrl;

    private final WebClient client;

    public StatsClient() {
        this.client = WebClient.create(baseUrl);
    }

    public void saveStats(String app, String uri, String ip, LocalDateTime timestamp) {
        final StatsHitDto endpointHit = new StatsHitDto(app, uri, ip, timestamp);
        log.info("Сохранение статистики {}", endpointHit);
        this.client.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHit, StatsHitDto.class)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public ResponseEntity<List<ViewStatsDto>> getStats(String start, String end, String[] uris, Boolean isUnique) {
        log.info("получена статистика за период с {}, по {}, uris {}, unique {}", start, end, uris, isUnique);
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stats")
                        .queryParam("start", start)
                        .queryParam("end", end)
                        .queryParam("uris", uris)
                        .queryParam("unique", isUnique)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntityList(ViewStatsDto.class)
                .block();
    }

}