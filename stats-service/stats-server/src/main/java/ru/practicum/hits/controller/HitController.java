package ru.practicum.hits.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.HitDto;
import ru.practicum.NewHitRequest;
import ru.practicum.ViewStatsDto;
import ru.practicum.hits.service.HitService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Validated
@Slf4j
public class HitController {

    private final HitService hitService;

    public HitController(HitService hitService) {
        this.hitService = hitService;
    }

    @PostMapping("/hit")
    public HitDto addHit(@Valid @RequestBody NewHitRequest request) {
        //log.info("Добавляется пользователь");
        return hitService.addHit(request);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam String start, @RequestParam String end,
                                       @RequestParam List<String> uris,
                                       @RequestParam(defaultValue = "false") boolean unique) {
        //log.info("Запрошен поиск вещи");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDate = LocalDateTime.parse(start, formatter);
        LocalDateTime endDate = LocalDateTime.parse(end, formatter);
        return hitService.getStats(startDate, endDate, uris, unique);
    }
}
