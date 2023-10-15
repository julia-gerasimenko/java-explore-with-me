package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.StatsHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exeption.ValidateException;
import ru.practicum.model.StatsHitMapper;
import ru.practicum.model.StatHit;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StatServiceImpl implements StatService {

    private final StatRepository repository;

    @Override
    public void saveStat(StatsHitDto dto) {
        StatHit statHit = repository.save(StatsHitMapper.statsHitDtoToStatHit(dto));
        log.info("Статистика сохранена {}", statHit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            log.info("Дата окончания не может быть раньше даты начала.");
            throw new ValidateException("Дата окончания не может быть раньше даты начала.");
        }

        if (uris.isEmpty()) {
            if (unique) {
                log.info("получена статистика, где isUnique {} ", unique);
                return repository.getStatsByUniqueIp(start, end);
            } else {
                log.info("получена статистика, где isUnique {} ", unique);
                return repository.getAllStats(start, end);
            }
        } else {
            if (unique) {
                log.info("получена статистика, где  isUnique {}, где uris {} ", unique, uris);
                return repository.getStatsByUrisByUniqueIp(start, end, uris);
            } else {
                log.info("получена статистика, где  isUnique {}, где uris {} ", unique, uris);
                return repository.getAllStatsByUris(start, end, uris);
            }
        }
    }

}
