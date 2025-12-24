package ru.practicum.participation;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.participation.dto.ParticipationDto;
import ru.practicum.participation.dto.ParticipationStatusRequest;
import ru.practicum.participation.dto.ParticipationStatusResult;

import java.util.List;

@Slf4j
@RestController
@RequestMapping
public class ParticipationController {

    private final ParticipationService participationService;

    public ParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }


    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationDto addParticipation(@PathVariable Long userId,
                                             @RequestParam(required = false) Long eventId) {
        if (eventId == null) {
            log.warn("Отсутствует обязательный параметр eventId");
            throw new ValidationException("Параметр eventId обязателен", "Обязательный параметр");
        }
        log.info("Запрос на создание заявки на участие: пользователь id={}, событие id={}", userId, eventId);
        return participationService.addParticipation(userId, eventId);
    }

    @GetMapping("/users/{userId}/requests")
    public List<ParticipationDto> getUserParticipation(@PathVariable Long userId) {
        log.info("Запрос на получение заявок пользователя: пользователя id={}", userId);
        return participationService.getUserParticipation(userId);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationDto> getEventParticipants(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Запрос на получение участников события: пользователь id={}, событие id={}", userId, eventId);
        return participationService.getEventParticipants(userId, eventId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationDto updateParticipationCancel(@PathVariable Long userId,
                                                      @PathVariable Long requestId) {
        log.info("Запрос на отмену заявки: пользователь id={}, заявка id={}", userId, requestId);
        return participationService.updateParticipationCancel(userId, requestId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public ParticipationStatusResult updateParticipationStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody ParticipationStatusRequest participationStatusRequest
    ) {
        log.info("Запрос на обновление статуса заявки: пользователь id={}, событие id={}", userId, eventId);
        return participationService.updateParticipationStatus(userId, eventId, participationStatusRequest);
    }

}
