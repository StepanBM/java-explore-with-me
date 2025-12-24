package ru.practicum.participation;

import ru.practicum.participation.dto.ParticipationDto;
import ru.practicum.participation.dto.ParticipationStatusRequest;
import ru.practicum.participation.dto.ParticipationStatusResult;

import java.util.List;

public interface ParticipationService {

    ParticipationDto addParticipation(Long userId, Long eventId);

    List<ParticipationDto> getUserParticipation(Long userId);

    List<ParticipationDto> getEventParticipants(Long userId, Long eventId);

    ParticipationDto updateParticipationCancel(Long userId, Long requestId);

    ParticipationStatusResult updateParticipationStatus(Long userId, Long eventId, ParticipationStatusRequest participationStatusRequest);
}
