package ru.practicum;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatsClient {

    private final RestClient restClient;

    public StatsClient(@Value("${stats-server.url:http://localhost:9090}") String url) {
        this.restClient = RestClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void addHit(NewHitRequest request) {
        try {
            restClient.post()
                    .uri("/hit")
                    .body(request) // Spring сам сериализует в JSON
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // Для отладки: логируем что отправляем
           // log.error("Failed to send hit: {}", request);
            throw e;
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Даты начала и окончания должны быть");
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Дата окончания должна быть позже даты начала");
        }

        // 1. Форматируем дату БЕЗ ПРОБЕЛА (используем стандартный ISO формат)
        // Сервис статистики ожидает формат "yyyy-MM-dd HH:mm:ss" но пробел может вызывать проблемы
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startStr = start.format(formatter);
        String endStr = end.format(formatter);

        // 2. НЕ кодируем строки! RestClient сам закодирует параметры
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/stats")
                .queryParam("start", startStr)
                .queryParam("end", endStr);

        if (uris != null && !uris.isEmpty()) {
            // Добавляем каждый URI как отдельный параметр
            for (String uri : uris) {
                uriBuilder.queryParam("uris", uri);
            }
        }

        if (unique != null) {
            uriBuilder.queryParam("unique", unique);
        }

        // 3. Получаем URI с правильным кодированием
        String uri = uriBuilder.build().toUriString();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ViewStatsDto>>() {});
    }

}