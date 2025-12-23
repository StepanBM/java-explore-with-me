package ru.practicum.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventRequest;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.exceptions.UpdateValidation;
import ru.practicum.exceptions.CreateValidation;

import java.time.LocalDateTime;
import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto addEvent(@PathVariable Long userId, @Validated(CreateValidation.class) @RequestBody NewEventRequest request) {
        log.info("Запрос на создание нового события: пользователь id={}, название='{}'", userId, request.getTitle());
        return eventService.addEvent(userId, request);
    }

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получение событий пользователя: пользователь id={}, from={}, size={}", userId, from, size);
        return eventService.getUserEvents(userId, from, size);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventDto getUserIdEventId(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    ) {
        log.info("Запрос на получение события: пользователь id={}, событие id={}", userId, eventId);
        return eventService.getUserIdEventId(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventDto updateEventUser(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @Validated(UpdateValidation.class) @RequestBody UpdateEventUserRequest request
    ) {
        log.info("Запрос на обновление события пользователем: пользователь id={}, событие id={}, данные={}", userId, eventId, request);
        return eventService.updateEventUser(userId, eventId, request);
    }

    @PatchMapping("/admin/events/{eventId}")
    public EventDto updateEventAdmin(
            @PathVariable("eventId") Long eventId,
            @Validated(UpdateValidation.class) @RequestBody UpdateEventUserRequest request
    ) {
        log.info("Запрос на административное обновление события: событие id={}, данные={}", eventId, request);
        return eventService.updateEventAdmin(eventId, request);
    }

    @GetMapping("/events/{id}")
    public EventDto getEventId(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        log.info("Публичный запрос на получение события: id={}, ip={}", id, request.getRemoteAddr());
        return eventService.getEventId(id, request.getRemoteAddr(), request.getRequestURI());
    }

    @GetMapping("/admin/events")
    public List<EventDto> getEventsAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Административный запрос на получение событий: users={}, states={}, categories={}, from={}, size={}",
                users, states, categories, from, size);
        return eventService.getEventsAdmin(
                users,
                states,
                categories,
                rangeStart,
                rangeEnd,
                from,
                size);
    }

    @GetMapping("/events")
    public List<EventShortDto> getEventsList(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        log.info("Публичный запрос на получение событий: текст='{}', категории={}, paid={}, from={}, size={}, ip={}",
                text, categories, paid, from, size, request.getRemoteAddr());
        return eventService.getEventsList(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size,
                request.getRequestURI(),
                request.getRemoteAddr()
        );
    }

}
