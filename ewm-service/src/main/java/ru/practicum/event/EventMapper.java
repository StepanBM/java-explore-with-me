package ru.practicum.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.category.Category;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventRequest;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.user.User;

import java.time.LocalDateTime;

import static ru.practicum.category.CategoryMapper.mapToCategoryDto;
import static ru.practicum.location.LocationMapper.mapToLocation;
import static ru.practicum.location.LocationMapper.mapToLocationDto;
import static ru.practicum.user.UserMapper.mapToUserShortDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    public static Event mapToEvent(NewEventRequest request, Category category, User initiator) {
        Event event = new Event();
        event.setAnnotation(request.getAnnotation());
        event.setCategory(category);
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setLocation(mapToLocation(request.getLocation()));
        event.setPaid(request.isPaid());
        event.setParticipantLimit(request.getParticipantLimit());
        event.setRequestModeration(request.getRequestModeration());
        event.setTitle(request.getTitle());

        // Устанавливаем начальные значения
        event.setInitiator(initiator);
        event.setCreatedOn(LocalDateTime.now());
        event.setConfirmedRequests(0);

        return event;
    }

    public static EventDto mapToEventDto(Event event) {
        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(mapToCategoryDto(event.getCategory()));
        dto.setConfirmedRequests(event.getConfirmedRequests());
        dto.setCreatedOn(event.getCreatedOn());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(mapToUserShortDto(event.getInitiator()));
        dto.setLocation(mapToLocationDto(event.getLocation()));
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        //dto.setPublishedOn(event.getPublishedOn());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState() != null ? event.getState().name() : null);
        dto.setTitle(event.getTitle());

        return dto;
    }

    public static EventShortDto mapToEventShortDto(Event event) {
        EventShortDto shortDto = new EventShortDto();
        shortDto.setId(event.getId());
        shortDto.setAnnotation(event.getAnnotation());
        shortDto.setCategory(mapToCategoryDto(event.getCategory()));
        shortDto.setConfirmedRequests(event.getConfirmedRequests());
        shortDto.setEventDate(event.getEventDate());
        shortDto.setInitiator(mapToUserShortDto(event.getInitiator()));
        shortDto.setPaid(event.getPaid());
        shortDto.setTitle(event.getTitle());
        //dto.setViews(event.getViews() != null ? event.getViews() : 0L);

        return shortDto;
    }

    public static void updateEventFields(Event event, UpdateEventUserRequest request) {
        if (request.hasTittle()) {
            event.setTitle(request.getTitle());
        }
        if (request.hasAnnotation()) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.hasDescription()) {
            event.setDescription(request.getDescription());
        }
        if (request.hasEventDate()) {
            event.setEventDate(request.getEventDate());
        }
        if (request.hasParticipantLimit()) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.hasLocation()) {
            event.setLocation(mapToLocation(request.getLocation()));
        }
        if (request.hasPaid()) {
            event.setPaid(request.getPaid());
        }
        if (request.hasRequestModeration()) {
            event.setRequestModeration(request.getRequestModeration());
        }
    }
}
