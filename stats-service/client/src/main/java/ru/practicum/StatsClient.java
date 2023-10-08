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

    private final WebClient client;

    public StatsClient(@Value("${stats.server.url}") String baseUrl) {
        this.client = WebClient.create(baseUrl);
    }

    public ResponseEntity<List<ViewStatsDto>> getStats(String start, String end, List<String> uris, Boolean unique) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stats")
                        .queryParam("start", start)
                        .queryParam("end", end)
                        .queryParam("uris", uris)
                        .queryParam("unique", unique)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntityList(ViewStatsDto.class)
                .doOnNext(c -> log.info("Get stats with param: start date {}, end date {}, uris {}, unique {}",
                        start, end, uris, unique))
                .block();
    }

    public void saveStats(String app, String uri, String ip, LocalDateTime timestamp) {

        this.client.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new StatsHitDto(app, uri, ip, timestamp))
                .retrieve()
                .toBodilessEntity()
                .doOnNext(c -> log.info("Save stats"))
                .block();
    }
}