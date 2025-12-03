package ru.practicum.hits.mapper;


import ru.practicum.HitDto;
import ru.practicum.NewHitRequest;
import ru.practicum.hits.model.Hit;


public final class HitMapper {

    public static Hit mapToHit(NewHitRequest request) {
        Hit hit = new Hit();
        hit.setApp(request.getApp());
        hit.setUri(request.getUri());
        hit.setIp(request.getIp());
        hit.setTimestamp(request.getTimestamp());

        return hit;
    }

    public static HitDto mapToHitDto(Hit hit) {
        HitDto dto = new HitDto();
        dto.setId(hit.getId());
        dto.setApp(hit.getApp());
        dto.setUri(hit.getUri());
        dto.setIp(hit.getIp());
        dto.setTimestamp(hit.getTimestamp());

        return dto;
    }
}
