package ru.practicum.hits.service;

import ru.practicum.HitDto;
import ru.practicum.NewHitRequest;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface HitService {

    HitDto addHit(NewHitRequest request);

    List<ViewStatsDto> getStats(LocalDateTime startDate, LocalDateTime endDate, List<String> uris, boolean unique);
}
