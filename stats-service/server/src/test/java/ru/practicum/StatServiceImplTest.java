package ru.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.StatsHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.StatHit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatServiceImplTest {
    @Mock
    private StatRepository repository;
    @InjectMocks
    private StatServiceImpl statService;

    private StatsHitDto statsHitDto;
    private StatHit statHit;
    private LocalDateTime time;
    private ViewStatsDto viewStatsDtoOne;

    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    public void beforeEach() {
        time = LocalDateTime.of(2022, 3, 5, 23, 0);
        start = LocalDateTime.of(2021, 2, 20, 12, 0);
        end = LocalDateTime.of(2023, 10, 30, 12, 10);

        statsHitDto = StatsHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/2")
                .ip("192.163.0.1")
                .timestamp(time)
                .build();

        statHit = StatHit.builder()
                .id(1)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("198.123.1.2")
                .timestamp(time)
                .build();

        viewStatsDtoOne = ViewStatsDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(2)
                .build();
    }

    @Test
    public void createEndpointHitTeat() {
        when(repository.save(any())).thenReturn(statHit);
        statService.saveStat(statsHitDto);

        verify(repository, times(1)).save(any());
    }

    @Test
    public void getStatsWhenUniqueTrueAndUrisEmptyTest() {
        List<String> uris = new ArrayList<>();
        List<ViewStatsDto> expected = List.of(viewStatsDtoOne);
        when(repository.getStatsByUniqueIp(any(), any())).thenReturn(expected);

        List<ViewStatsDto> actual = statService.getStats(start, end, uris, true);

        assertNotNull(actual);
        assertNotNull(actual.get(0).getApp());
        assertNotNull(actual.get(0).getUri());

        verify(repository, times(1)).getStatsByUniqueIp(any(), any());
    }

    @Test
    public void getStatsWhenUniqueFalseAndUrisEmptyTest() {
        List<String> uris = new ArrayList<>();
        List<ViewStatsDto> expected = List.of(viewStatsDtoOne);
        when(repository.getAllStats(any(), any())).thenReturn(expected);

        List<ViewStatsDto> actual = statService.getStats(start, end, uris, false);

        assertNotNull(actual);
        assertNotNull(actual.get(0).getApp());
        assertNotNull(actual.get(0).getUri());

        verify(repository, times(1)).getAllStats(any(), any());
    }


    @Test
    public void getStatsWhenUniqueTruedAndUrisNotEmptyTest() {
        List<String> uris = List.of("1", "2");
        List<ViewStatsDto> expected = List.of(viewStatsDtoOne);
        when(repository.getStatsByUrisByUniqueIp(any(), any(), any())).thenReturn(expected);

        List<ViewStatsDto> actual = statService.getStats(start, end, uris, true);

        assertNotNull(actual);
        assertNotNull(actual.get(0).getApp());
        assertNotNull(actual.get(0).getUri());

        verify(repository, times(1)).getStatsByUrisByUniqueIp(any(), any(), any());
    }

    @Test
    public void getStatsWhenUniqueAndUrisNotEmptyTest() {
        List<String> uris = List.of("1", "2");
        List<ViewStatsDto> expected = List.of(viewStatsDtoOne);
        when(repository.getAllStatsByUris(any(), any(), any())).thenReturn(expected);

        List<ViewStatsDto> actual = statService.getStats(start, end, uris, false);

        assertNotNull(actual);
        assertNotNull(actual.get(0).getApp());
        assertNotNull(actual.get(0).getUri());

        verify(repository, times(1)).getAllStatsByUris(any(), any(), any());
    }
}