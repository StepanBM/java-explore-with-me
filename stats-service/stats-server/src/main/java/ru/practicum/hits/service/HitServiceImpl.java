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
       // log.debug("Начинается добавление пользователя по запросу {}", request);
        Hit hit = mapToHit(request);
       // log.debug("Запрос на добавление пользователя конвертирован в объект класса User {}", hit);
        hit = hitRepository.save(hit);
       // log.debug("Добавлен пользователь {}", hit);
       return mapToHitDto(hit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (unique) {
            if (uris != null && !uris.isEmpty()) {
                return hitRepository.findUniqueHitsForUris(start, end, uris);
            } else {
                return hitRepository.findUniqueHits(start, end);
            }
        } else {
            if (uris != null && !uris.isEmpty()) {
                return hitRepository.findHitsForUris(start, end, uris);
            } else {
                return hitRepository.findHits(start, end);
            }
        }
    }
}
