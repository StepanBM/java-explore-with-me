package ru.practicum.hits.service;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.HitDto;
import ru.practicum.NewHitRequest;
import ru.practicum.ViewStatsDto;
import ru.practicum.hits.repository.HitRepository;
import ru.practicum.hits.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.hits.mapper.HitMapper.mapToHit;
import static ru.practicum.hits.mapper.HitMapper.mapToHitDto;

@Slf4j
@Service
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;

    @Autowired
    public HitServiceImpl(HitRepository hitRepository) {
        this.hitRepository = hitRepository;
    }

    @Override
    @Transactional
    public HitDto addHit(NewHitRequest request) {
        log.debug("Начинается сохранение информации о том, что к эндпоинту был запрос по запросу {}", request);
        Hit hit = mapToHit(request);
        log.debug("Запрос на сохранение информации о том, что к эндпоинту был запрос по запросу конвертирован в объект класса Hit {}", hit);
        hit = hitRepository.save(hit);
        log.debug("Сохранена информации о том, что к эндпоинту был запрос {}", hit);
       return mapToHitDto(hit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.debug("Начинается получение статистики по посещениям");

        if (start == null) {
            log.warn("Начальная дата не может быть null");
            throw new IllegalArgumentException("Начальная дата не может быть null");
        }

        if (end == null) {
            log.warn("Конечная дата не может быть null");
            throw new IllegalArgumentException("Конечная дата не может быть null");
        }

        // Начальная дата не может быть раньше конечной
        if (start.isAfter(end)) {
            log.warn("Начальная дата {} не может быть позже конечной даты {}", start, end);
            throw new IllegalArgumentException("Начальная дата не может быть позже конечной даты");
        }

        if (unique) {
            if (uris != null && !uris.isEmpty()) {
                log.debug("Получение уникальных хитов по заданному Uri при unique=true");
                return hitRepository.findUniqueHitsUris(start, end, uris);
            } else {
                log.debug("Получение всех уникальных хитов при unique=true");
                return hitRepository.findUniqueHits(start, end);
            }
        } else {
            if (uris != null && !uris.isEmpty()) {
                log.debug("Получение уникальных хитов по заданному Uri при unique=false");
                return hitRepository.findHitsUris(start, end, uris);
            } else {
                log.debug("Получение всех уникальных хитов при unique=false");
                return hitRepository.findHits(start, end);
            }
        }
    }
}
