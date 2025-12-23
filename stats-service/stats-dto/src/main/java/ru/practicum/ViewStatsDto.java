package ru.practicum;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ViewStatsDto {
    private String app;
    private String uri;
    private Long hits;

//    public ViewStatsDto(String app, String uri, Long hits) {
//        this.app = app;
//        this.uri = uri;
//        this.hits = hits;
//    }

}
