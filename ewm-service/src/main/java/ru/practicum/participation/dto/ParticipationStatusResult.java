package ru.practicum.participation.dto;

import lombok.Data;

import java.util.List;

@Data
public class ParticipationStatusResult {

    // Список подтвержденных заявок
    private List<ParticipationDto> confirmedRequests;

    // Список отклоненных заявок
    private List<ParticipationDto> rejectedRequests;

    public ParticipationStatusResult(List<ParticipationDto> list, List<ParticipationDto> list1) {
    }
}
