package ru.practicum;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

        restClient.post()
                .uri("/hit")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Даты начала и окончания должны быть заданы");
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Даты окончания должна быть позже даты начала");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startStr = URLEncoder.encode(start.format(formatter), StandardCharsets.UTF_8);
        String endStr = URLEncoder.encode(end.format(formatter), StandardCharsets.UTF_8);

        StringBuilder uriBuilder = new StringBuilder("/stats?");
        uriBuilder.append("start=").append(startStr);
        uriBuilder.append("&end=").append(endStr);

        if (uris != null && !uris.isEmpty()) {
            String urisParam = String.join(",", uris);
            urisParam = URLEncoder.encode(urisParam, StandardCharsets.UTF_8);
            uriBuilder.append("&uris=").append(urisParam);
        }

        if (unique != null) {
            uriBuilder.append("&unique=").append(unique);
        }

        return restClient.get()
                .uri(uriBuilder.toString())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ViewStatsDto>>() {});
    }

}