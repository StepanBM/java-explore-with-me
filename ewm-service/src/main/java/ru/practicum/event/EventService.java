package ru.practicum.event;

import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventRequest;
import ru.practicum.event.dto.UpdateEventUserRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventDto addEvent(Long userId, NewEventRequest request);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventDto getUserIdEventId(Long userId, Long eventId);

    EventDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest request);

    EventDto updateEventAdmin(Long eventId, UpdateEventUserRequest request);

    EventDto getEventId(Long eventId, String ip, String uri);

    List<EventShortDto> getEventsList(String text, List<Long> categories, Boolean paid,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                      Boolean onlyAvailable, String sort, int from, int size,
                                      String addressIp, String requestUri);

    List<EventDto> getEventsAdmin(List<Long> users, List<String> states, List<Long> categories,
                                  LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

}
