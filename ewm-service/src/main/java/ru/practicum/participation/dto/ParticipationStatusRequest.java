package ru.practicum.participation.dto;

import lombok.Data;
import ru.practicum.state.ParticipationStatus;

import java.util.List;

@Data
public class ParticipationStatusRequest {

    // Идентификаторы запросов на участие в событии текущего пользователя
    private List<Long> requestIds;

    // Новый статус запроса на участие в событии текущего пользователя
    private ParticipationStatus status;
}
