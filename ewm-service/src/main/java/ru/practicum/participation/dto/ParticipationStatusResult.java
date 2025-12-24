package ru.practicum.participation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationStatusResult {

    // Список подтвержденных заявок
    private List<ParticipationDto> confirmedRequests;

    // Список отклоненных заявок
    private List<ParticipationDto> rejectedRequests;

}
