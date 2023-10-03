package ru.practicum;

import ru.practicum.dto.StatsHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatService {

    void saveStat(StatsHitDto statsHitDto);

    Collection<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}