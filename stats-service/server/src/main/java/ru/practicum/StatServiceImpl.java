package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.StatsHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exeption.ValidationException;
import ru.practicum.model.StatHit;
import ru.practicum.model.StatsHitMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatServiceImpl implements StatService {

    private final StatRepository repository;

    @Transactional
    @Override
    public void saveStat(StatsHitDto dto) {
        StatHit statHit = repository.save(StatsHitMapper.statsHitDtoToStatHit(dto));
        log.info("Получена статистика {}", statHit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            log.info("Время окончания не может быть раньше времени начала");
            throw new ValidationException("Время окончания не может быть раньше времени начала");
        }

        if (uris.isEmpty()) {
            if (unique) {
                log.info("Получить всю статистику где isUnique {} ", unique);
                return repository.getStatsByUniqueIp(start, end);
            } else {
                log.info("Получить всю статистику где isUnique {} ", unique);
                return repository.getAllStats(start, end);
            }
        } else {
            if (unique) {
                log.info("Получить всю статистику где isUnique {} и uris {} ", unique, uris);
                return repository.getStatsByUrisByUniqueIp(start, end, uris);
            } else {
                log.info("Получить всю статистику где isUnique {} и uris {} ", unique, uris);
                return repository.getAllStatsByUris(start, end, uris);
            }
        }
    }
}
