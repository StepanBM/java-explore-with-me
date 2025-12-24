package ru.practicum.participation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.participation.dto.ParticipationDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParticipationMapper {

    public static ParticipationDto mapToParticipationDto(Participation participation) {
        ParticipationDto dto = new ParticipationDto();
        dto.setId(participation.getId());
        dto.setEvent(participation.getEvent().getId());
        dto.setRequester(participation.getRequester().getId());
        dto.setStatus(participation.getStatus().name());
        dto.setCreated(participation.getCreated());

        return dto;
    }
}
