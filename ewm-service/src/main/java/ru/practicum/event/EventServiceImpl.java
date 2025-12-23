package ru.practicum.event;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.NewHitRequest;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventRequest;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.exceptions.*;
import ru.practicum.state.EventState;
import ru.practicum.state.EventStateAction;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.event.EventMapper.*;

@Service
@Slf4j
public class EventServiceImpl implements EventService {

    @Value("${app.name}")
    private String appName;

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    private final StatsClient statsClient;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository,
                            UserRepository userRepository,
                            CategoryRepository categoryRepository,
                            StatsClient statsClient,
                            @Value("${app.name}") String appName) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.statsClient = statsClient;
        this.appName = appName;
    }

    @Override
    @Transactional
    public EventDto addEvent(Long userId, NewEventRequest request) {
        log.info("Создание события для пользователя id={} с данными: {}", userId, request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске пользователя. Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден");
                });

        Category category = categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске категории. Категория с id={} не найдена", request.getCategory());
                    return new NotFoundException("Категория с id=" + request.getCategory() + " не найдена");
                });

        // Проверка даты события, она должна быть начата минимум через 2 часа
//        if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
//            log.warn("Ошибка дата начала события должна быть не ранее чем за 2 часа от текущего момента");
//            throw new ErrorEventDateException("Дата начала события должна быть не ранее чем за 2 часа от текущего момента");
//        }

        Event event = mapToEvent(request, category, user);
        event.setState(EventState.PENDING);
        event = eventRepository.save(event);
        log.debug("Создано новое событие: id={}, title={}, user={}", event.getId(), event.getTitle(), userId);

        return mapToEventDto(event);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        log.info("Получение событий для пользователя с id: {}, from: {}, size: {}", userId, from, size);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске пользователя. Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден");
                });

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);

        return events.stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventDto getUserIdEventId(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске пользователя. Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден");
                });

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске события. Событие с id={} не найдено", eventId);
                    return new NotFoundException("Событие с id=" + eventId + " не найдено");
                });

        // Получаем количество просмотров
        Long views = getEventView(eventId);
        log.debug("Получаем количество просмотров views=" + views);

        EventDto eventDto = mapToEventDto(event);
        eventDto.setViews(views);

        return mapToEventDto(event);
    }

    @Override
    @Transactional
    public EventDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        log.info("Обновление события пользователем: userId={}, eventId={}, request={}", userId, eventId, request);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске события. Событие с id={} не найдено", eventId);
                    return new NotFoundException("Событие с id=" + eventId + " не найдено");
                });

        // Проверка возможности изменения события
        // Проверка опубликованно ли событие
        if (event.getState() == EventState.PUBLISHED) {
            log.warn("Ошибка измененить уже опубликованное событие нельзя: eventId={}", event.getId());
            throw new ConflictErrorException("Запрещено изменять опубликованное уже событие");
        }

        // Проверка даты события, она должна быть начата минимум через 2 часа
        LocalDateTime latestUpdateTime = event.getEventDate().minusHours(2);
        if (LocalDateTime.now().isAfter(latestUpdateTime)) {
            log.warn("Ошибка дата начала события должна быть не ранее чем за 2 часа от текущего момента");
            throw new ErrorEventDateException("Дата начала события должна быть не ранее чем за 2 часа от текущего момента");
        }

        // Обновление категорию, если указана в запросе
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> {
                        log.warn("Ошибка при поиске категории. Категория с id={} не найдена", request.getCategory());
                        return new NotFoundException("Категория с id=" + request.getCategory() + " не найдена");
                    });
            event.setCategory(category);
        }

        updateEventFields(event, request);

        // Обновляем дату события
        if (request.getEventDate() != null) {
            LocalDateTime minAllowedDate = LocalDateTime.now().plusHours(2);
            if (request.getEventDate().isBefore(minAllowedDate)) {
                log.warn("Ошибка дата начала события должна быть не ранее чем за 2 часа от текущего момента");
                throw new ErrorEventDateException("Дата начала события должна быть не ранее чем за 2 часа от текущего момента");
            }
            event.setEventDate(request.getEventDate());
        }

        // Изменение статуса события
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    log.debug("Событие отправлено на рассмотрение: eventId={}", event.getId());
                    break;

                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    log.debug("Событие отменено");
                    break;

                default:
                    log.warn("Недопустимое действие со статусом");
                    throw new IllegalArgumentException("Данного статуса не существует");
            }
        }

        Event updatedEvent = eventRepository.save(event);

        log.debug("Событие успешно обновлено: id={}, title={}, state={}", updatedEvent.getId(), updatedEvent.getTitle(), updatedEvent.getState());

        EventDto eventDto = mapToEventDto(event);

        // Получаем количество просмотров только если событие опубликовано
        if (updatedEvent.getState() == EventState.PUBLISHED) {
            Long views = getEventView(eventId);
            eventDto.setViews(views);
        }

        return mapToEventDto(updatedEvent);

    }

    @Override
    @Transactional
    public EventDto updateEventAdmin(Long eventId, UpdateEventUserRequest request) {
        log.info("Запрос на обновление события администратором: eventId={}, request={}", eventId, request);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске события. Событие с id={} не найдено", eventId);
                    return new NotFoundException("Событие с id=" + eventId + " не найдено");
                });

        // Проверка даты события
        if (request.getEventDate() != null) {
            LocalDateTime newEventDate = request.getEventDate();

            if (newEventDate.isBefore(LocalDateTime.now())) {
                log.warn("Ошибка дата события не может быть в прошедшем времени: eventId={}, newDate={}", eventId, newEventDate);
                throw new ErrorEventDateException("Дата события не может быть в прошедшем времени");
            }

            // Проверка даты события, она должна быть начата минимум через 1 часа
            if (request.getStateAction() == EventStateAction.PUBLISH_EVENT) {
                LocalDateTime minPublishTime = LocalDateTime.now().plusHours(1);
                if (newEventDate.isBefore(minPublishTime)) {
                    log.warn("Ошибка дата события должна быть не ранее, чем через час от момента публикации: eventId={}, eventDate={}, minTime={}", eventId, newEventDate, minPublishTime);
                    throw new ErrorEventDateException("Дата события должна быть не ранее, чем через час от момента публикации");
                }
            }
        }

        // Обработка действий со статусом
        if (request.getStateAction() != null) {
            if (request.getStateAction() == EventStateAction.PUBLISH_EVENT) {

                // Нельзя опубликовать уже опубликованное событие
                if (event.getState() == EventState.PUBLISHED) {
                    log.warn("Ошибка нельзя опубликовать уже опубликованное событие: eventId={}", eventId);
                    throw new ConflictErrorException("Данное событие уже опубликовано");
                }

                // Нельзя опубликовать отмененное событие
                if (event.getState() == EventState.CANCELED) {
                    log.warn("Ошибка нельзя опубликовать уже отмененное события: eventId={}", eventId);
                    throw new ConflictErrorException("Нельзя опубликовать отмененное событие");
                }

                // Разрешается публиковать события в состоянии PENDING
                if (event.getState() != EventState.PENDING) {
                    log.warn("Ошибка публикации событие с недопустимым статусом: eventId={}, currentState={}. Стутус должен быть PENDING", eventId, event.getState());
                    throw new ConflictErrorException("Публиковать можно только события со статусом PENDING");
                }

                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                log.debug("Событие опубликовано: eventId={}, publishTime={}", eventId, event.getPublishedOn());

            } else if (request.getStateAction() == EventStateAction.REJECT_EVENT) {

                // Нельзя отменить уже опубликованное событие
                if (event.getState() == EventState.PUBLISHED) {
                    log.warn("Ошибка нельзя отклонить уже опубликованное событие: eventId={}", eventId);
                    throw new ConflictErrorException("Запрещено отклонять уже опубликованное событие");
                }

                // Нельзя отклонять уже отмененно событие
                if (event.getState() == EventState.CANCELED) {
                    log.warn("Событие уже отменено: eventId={}", eventId);
                    throw new ConflictErrorException("Событие уже отменено");
                }

                event.setState(EventState.CANCELED);
                log.info("Событие отменено: eventId={}", eventId);
            }
        }

        updateEventFields(event, request);

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие обновлено: eventId={}, newState={}", eventId, updatedEvent.getState());

        // Получаем количество просмотров
        Long views = getEventView(eventId);

        EventDto eventDto = mapToEventDto(updatedEvent);
        eventDto.setViews(views);

        log.debug("Возвращаем dto обновленного события: eventId={}, views={}", eventId, views);
        return eventDto;
    }

    @Override
    public EventDto getEventId(Long id, String remoteAddr, String requestUri) {
        log.info("Получение события с id: {} от ip: {}", id, remoteAddr);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске события. Событие с id={} не найдено", id);
                    return new NotFoundException("Событие с id=" + id + " не найдено");
                });

        if (event.getState() != EventState.PUBLISHED) {
            log.warn("Ошибка событие id={} еще не опубликованно", id);
            throw new EventNotPublishedException("Событие еще не опубликовано");
        }

        // Добавляем запись о просмотре в статистику
        addHit(remoteAddr, requestUri);

        // Получаем количество просмотров
        Long views = getEventView(id);

        EventDto eventDto = mapToEventDto(event);
        eventDto.setViews(views);

        log.debug("Событие с id={} получено, количество просмотров: {}", id, views);
        return eventDto;
    }

    public List<EventShortDto> getEventsList(String text, List<Long> categories, Boolean paid,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                             Boolean onlyAvailable, String sort, int from, int size,
                                             String addressIp, String requestUri) {

        if (rangeStart != null && rangeEnd != null && !rangeStart.isBefore(rangeEnd)) {
            log.warn("Некорректный диапазон дат: rangeStart={}, rangeEnd={}", rangeStart, rangeEnd);
            throw new ConstraintViolationException("Дата старта фильтра должна быть раньше даты окончания фильтра");
        }

        Pageable pageable;
        int pageNumber = from / size;
        int pageSize = size;
        if (sort == null) {
            log.debug("Сортировка по умолчанию: id по возрастанию");
            Sort sortByIdAsc = Sort.by("id").ascending();
            pageable = PageRequest.of(pageNumber, pageSize, sortByIdAsc);
        } else if (sort.equalsIgnoreCase("VIEWS")) {
            log.debug("Сортировка по просмотрам views по возрастанию");
            Sort sortByViewsAsc = Sort.by("views").ascending();
            pageable = PageRequest.of(pageNumber, pageSize, sortByViewsAsc);
        } else if (sort.equalsIgnoreCase("EVENT_DATE")) {
            log.debug("Сортировка по дате события eventDate по возрастанию");
            Sort sortByEventDateAsc = Sort.by("eventDate").ascending();
            pageable = PageRequest.of(pageNumber, pageSize, sortByEventDateAsc);
        } else {
            log.warn("Некорректный вариант сортировки: {}", sort);
            throw new NotFoundException("Указан некорректный вариант сортировки");
        }

        // Добавляем запись о просмотре в статистику
        log.debug("Добавление записи о просмотре: requestUri={}, addressIp={}", requestUri, addressIp);
        addHit(requestUri, addressIp);

        // Установка стартовой даты, если не указана
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
            log.debug("Параметр rangeStart не указан, установлен текущий момент времени: {}", rangeStart);
        } else {
            log.debug("Используется указанный диапазон: rangeStart={}, rangeEnd={}", rangeStart, rangeEnd);
        }

        log.debug("Вызов метода поиска событий с фильтрами");
        List<Event> events = eventRepository.findEvents(
                text == null ? null : text.toLowerCase(),
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                EventState.PUBLISHED,
                pageable
        );

        log.debug("Найдено {} событий", events.size());
        return events.stream().map(EventMapper::mapToEventShortDto).toList();
    }

    public List<EventDto> getEventsAdmin(List<Long> users, List<String> states, List<Long> categories,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        log.info("Получение событий для администратора: users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        List<EventState> eventStates = null;
        if (states != null) {
            eventStates = states.stream()
                    .map(EventState::valueOf)
                    .toList();
        }

        List<Event> events = eventRepository.findAdminEvents(users, eventStates, categories, rangeStart, rangeEnd, pageable);

        log.debug("Получение просмотров для событий");
        Map<Long, Long> views = getEventsViews(events);

        return events.stream()
                .map(event -> {
                    EventDto eventDto = EventMapper.mapToEventDto(event);
                    // Устанавливаем количество просмотров из мапы
                    eventDto.setViews(views.getOrDefault(event.getId(), 0L));
                    return eventDto;
                })
                .collect(Collectors.toList());
    }

    // Получение количества просмотров для списка событий
    private Map<Long, Long> getEventsViews(List<Event> events) {

        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            log.debug("Получение просмотров для {} событий", events.size());

            // Формируем список uri для всех событий
            List<String> uris = events.stream()
                    .map(event -> "/events/" + event.getId())
                    .collect(Collectors.toList());

            // Время создания самого первого события в приложении
            LocalDateTime appCreationDate = LocalDateTime.parse("2025-01-01 00:00:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Делаем запрос к сервису статистики для всех событий
            List<ViewStatsDto> stats = statsClient.getStats(
                    appCreationDate,
                    LocalDateTime.now(),
                    uris,
                    true
            );

            Map<Long, Long> eventViews = new HashMap<>();

            for (ViewStatsDto stat : stats) {

                // Извлекаем id события из uri (например: "/events/123" -> 123)
                String uri = stat.getUri();
                Long eventId = Long.parseLong(uri.substring("/events/".length()));
                eventViews.put(eventId, stat.getHits());

            }
            log.debug("Получены просмотры для {} из {} событий", eventViews.size(), events.size());

            return eventViews;

        } catch (Exception e) {
            log.warn("Ошибка при получении статистики просмотров для списка событий: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    // Отправка информацию о просмотре события в сервис клиента
    private void addHit(String ip, String uri) {
        try {
            log.info("Отправка информации о просмотре события: uri={}, ip={}", uri, ip);

            NewHitRequest request = NewHitRequest.builder()
                    .app(appName)
                    .uri(uri)
                    .ip(ip)
                    .timestamp(LocalDateTime.now())
                    .build();

            statsClient.addHit(request);
            log.debug("Информация о просмотре успешно отправлена");
        } catch (Exception e) {
            log.warn("Ошибка при отправке статистики просмотра для uri={}, ip={}: {}", uri, ip, e.getMessage(), e);
        }
    }

    // Получение количества просмотров событий
    private Long getEventView(Long eventId) {

        log.debug("Получение количества просмотров для события ID={}", eventId);

        LocalDateTime appCreationDate = LocalDateTime.parse("2025-11-30 12:00:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Формируем uri события для поиска в статистике
        List<String> uris = List.of("/events/" + eventId);
        log.debug("Параметры запроса статистики: start={}, uris={}", appCreationDate, uris);

        try {
            // Запрашиваем статистику - учитываем только уникальные ip
            List<ViewStatsDto> stats = statsClient.getStats(
                    appCreationDate,
                    LocalDateTime.now(),
                    uris,
                    true
            );

            log.debug("Получена статистика: {}", stats);

            Long views = stats.isEmpty() ? 0L : stats.get(0).getHits();
            log.debug("Количество просмотров для события ID={}: {}", eventId, views);

            return views;
        } catch (Exception e) {
            log.warn("Ошибка в statsClient.getStats() для события ID={}: {}", eventId, e.getMessage(), e);

            return 0L;
        }
    }

}
