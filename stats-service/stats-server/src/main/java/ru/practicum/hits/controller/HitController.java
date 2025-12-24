package ru.practicum.hits.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.HitDto;
import ru.practicum.NewHitRequest;
import ru.practicum.ViewStatsDto;
import ru.practicum.hits.service.HitService;
import ru.practicum.validation.CreateValidation;


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
    @ResponseStatus(HttpStatus.CREATED)
    public HitDto addHit(@Validated(CreateValidation.class) @RequestBody NewHitRequest request) {
        log.info("Сохраняется информации о том, что к эндпоинту был запрос");
        return hitService.addHit(request);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam String start, @RequestParam String end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Время начала и конца переводится в определенный формат");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDate = LocalDateTime.parse(start, formatter);
        LocalDateTime endDate = LocalDateTime.parse(end, formatter);
        log.info("Получение статистики по посещениям");
        return hitService.getStats(startDate, endDate, uris, unique);
    }
}
