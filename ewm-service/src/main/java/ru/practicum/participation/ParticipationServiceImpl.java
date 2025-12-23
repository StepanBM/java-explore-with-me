package ru.practicum.participation;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exceptions.ConflictErrorException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.participation.dto.ParticipationDto;
import ru.practicum.participation.dto.ParticipationStatusRequest;
import ru.practicum.participation.dto.ParticipationStatusResult;
import ru.practicum.state.EventState;
import ru.practicum.state.ParticipationStatus;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.participation.ParticipationMapper.mapToParticipationDto;

@Service
@Slf4j
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public ParticipationServiceImpl(ParticipationRepository participationRepository,
                                    UserRepository userRepository,
                                    EventRepository eventRepository) {
        this.participationRepository = participationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public ParticipationDto addParticipation(Long userId, Long eventId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске пользователя. Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден");
                });

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске события. Событие с id={} не найдено", eventId);
                    return new NotFoundException("Событие с id=" + eventId + " не найдено");
                });

        // Проверка, опубликовано ли событие
        if (event.getState() != EventState.PUBLISHED) {
            log.warn("Ошибка при попытке участия в неопубликованном событии с id={}", eventId);
            throw new ConflictErrorException("Запрещено участие в неопубликованном событии");
        }

        // Проверка, является ли пользователь инициатором события
        if (event.getInitiator().getId().equals(userId)) {
            log.warn("Ошибка инициатор события с id={} пытается подать заявку на участие", userId);
            throw new ConflictErrorException("Инициатор события не может подавать заявку на участие в собственном событии");
        }

        boolean existingParticipation = participationRepository.existsByRequesterIdAndEventId(userId, eventId);
        if (existingParticipation) {
            log.warn("Ошибка повторной заявки на участие: userId={}, eventId={}", userId, eventId);
            throw new ConflictErrorException("Этот пользователь уже отправил заявку на участие в этом событии");
        }

        int limit = event.getParticipantLimit();

        if (limit > 0 && limit <= participationRepository.countParticipationByEventAndStatus(event.getId(), ParticipationStatus.CONFIRMED)) {
            log.warn("Ошибка достигнут максимальный лимит участников события");
            throw new ConflictErrorException("Достигнут максимальный лимит участников события");
        }

        Participation participation = new Participation();
        participation.setEvent(event);
        participation.setRequester(user);
        participation.setStatus(ParticipationStatus.PENDING);
        participation.setCreated(LocalDateTime.now());

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            log.debug("Модерация заявок на участие в событии с id={} не требуется", eventId);
            participation.setStatus(ParticipationStatus.CONFIRMED);
        }

        if (limit == 0) {
            log.debug("Лимит участников равен 0, заявка подтверждается");
            participation.setStatus(ParticipationStatus.CONFIRMED);
        }

        if (participation.getStatus() == ParticipationStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
            log.debug("Количество подтвержденных заявок увеличено, обновлено событие");
        }

        Participation savedParticipation = participationRepository.save(participation);
        log.debug("Заявка сохранена: {}", savedParticipation);

        return mapToParticipationDto(savedParticipation);
    }

    @Override
    public List<ParticipationDto> getUserParticipation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске пользователя. Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден");
                });
        log.debug("Получение информации о заявках на участие пользователя с id={}", userId);
        List<Participation> participations = participationRepository.findAllByRequester(user);

        return participations.stream()
                .map(ParticipationMapper::mapToParticipationDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParticipationDto> getEventParticipants(Long userId, Long eventId) {
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

        if (!event.getInitiator().getId().equals(user.getId())) {
            log.warn("Ошибка запрос может просматривать только инициатор события");
            throw new ConflictErrorException("Запросы может просматривать только инициатор события");
        }

        List<Participation> participations = participationRepository.findAllByEvent(event);

        return participations.stream()
                .map(ParticipationMapper::mapToParticipationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationDto updateParticipationCancel(Long userId, Long requestId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске пользователя. Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден");
                });
        Participation participation = participationRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске запроса. Запрос с id={} не найден", requestId);
                    return new NotFoundException("Запрос с id=" + requestId + " не найден");
                });

        if (!participation.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Запрос с id=" + requestId + " не принадлежит пользователю с id=" + userId);
        }

        participation.setStatus(ParticipationStatus.CANCELED);
        participation = participationRepository.save(participation);
        log.info("Отмена запроса на участие с id={} пользователя с id={}", requestId, userId);

        return mapToParticipationDto(participation);
    }

    @Override
    @Transactional
    public ParticipationStatusResult updateParticipationStatus(Long userId, Long eventId, ParticipationStatusRequest participationStatusRequest) {
        log.info("Обновление статуса заявок: userId={}, eventId={}, request={}", userId, eventId, participationStatusRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске пользователя. Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден");
                });

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске события. Событие с id={} не найдено", eventId);
                    return new NotFoundException("Событие с id=" + eventId + " не найдено");
                });

        // Проверка прав инициатора
        if (!event.getInitiator().getId().equals(userId)) {
            log.warn("Ошибка изменение заявок возможна только инициатором: initiatorId={}, userId={}", event.getInitiator().getId(), userId);
            throw new ConflictErrorException("Статусы запросов может менять только инициатор события");
        }

        // Проверка необходимости модерации
        if (event.getParticipantLimit() == 0 && !event.getRequestModeration()) {
            throw new ConflictErrorException("Модерация заявок не требуется для данного события");
        }

        List<Long> requestIds = participationStatusRequest.getRequestIds();
        if (requestIds == null || requestIds.isEmpty()) {
            log.warn("Ошибка список ids заявок пустой: eventId={}", eventId);
            throw new NotFoundException("Список заявок пуст");
        }

        List<Participation> participations = participationRepository.findAllById(requestIds);
        if (participations.isEmpty()) {
            log.warn("Ошибка заявки. Заявки не найдены");
            throw new NotFoundException("Список заявок пуст");
        }

        // Проверка принадлежности заявок к событию
        for (Participation participation : participations) {
            if (!participation.getEvent().getId().equals(eventId)) {
                log.warn("Ошибка заявка не принадлежит событию: requestId={}, eventId={}, expectedEventId={}", participation.getId(), participation.getEvent().getId(), eventId);
                throw new ConflictErrorException("Заявка с id=" + participation.getId() + " не принадлежит событию с id=" + eventId);
            }
        }

        // Проверка текущего статуса заявок и подготовка списков для обработки
        List<Participation> participationStatusPending = new ArrayList<>();
        List<Participation> participationList = new ArrayList<>();

        for (Participation participation : participations) {
            if (participation.getStatus() == ParticipationStatus.PENDING) {
                participationStatusPending.add(participation);
            } else {
                participationList.add(participation);
                // Нельзя отменять подтвержденные заявки
                if (participationStatusRequest.getStatus() == ParticipationStatus.REJECTED && participation.getStatus() == ParticipationStatus.CONFIRMED) {
                    log.warn("Ошибка отменить уже принятую заявку нельзя: requestId={}, status=CONFIRMED", participation.getId());
                    throw new ConflictErrorException("Запрещено отменять уже принятую заявку с id=" + participation.getId());
                }
            }
        }

        if (participationStatusPending.isEmpty()) {
            log.warn("Ошибка заявок со статусом PENDING для обработки нет: eventId={}", eventId);
            throw new ConflictErrorException("Все заявки обработаны");
        }

        // Получение текущего количества подтвержденных заявок
        long confirmedParticipation = participationRepository.countByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED);
        long participantLimit = event.getParticipantLimit();
        ParticipationStatus participationNewStatus = participationStatusRequest.getStatus();

        // Проверка лимита при подтверждении
        if (participationNewStatus == ParticipationStatus.CONFIRMED) {
            if (participantLimit > 0 && confirmedParticipation >= participantLimit) {
                log.warn("Ошибка лимит участников достигнут максимума: eventId={}, limit={}, current={}", eventId, participantLimit, confirmedParticipation);
                throw new ConflictErrorException("На данном событии лимит участников достигнут своего максимума");
            }
            long confirmations = confirmedParticipation + participationStatusPending.size();
            if (participantLimit > 0 && confirmations > participantLimit) {
                log.warn("Ошибка лимита при подтверждении превышен: eventId={}, limit={}, current={}, requested={}", eventId, participantLimit, confirmedParticipation, participationStatusPending.size());
                throw new ConflictErrorException("Подтверждение заявок превысил participantLimit=" + participantLimit + " участников");
            }
        }

        // Обработка заявок
        List<Participation> participationsStatusConfirmed = new ArrayList<>();
        List<Participation> participationsStatusRejected = new ArrayList<>();

        if (participationNewStatus == ParticipationStatus.CONFIRMED) {
            for (Participation participation : participationStatusPending) {
                if (participantLimit == 0 || confirmedParticipation < participantLimit) {
                    participation.setStatus(ParticipationStatus.CONFIRMED);
                    participationsStatusConfirmed.add(participation);
                    confirmedParticipation++;
                    log.debug("Заявка подтверждена: requestId={}, eventId={}", participation.getId(), eventId);
                } else {
                    participation.setStatus(ParticipationStatus.REJECTED);
                    participationsStatusRejected.add(participation);
                    log.debug("Заявка отклонена из-за лимита: requestId={}, eventId={}", participation.getId(), eventId);
                }
            }
        } else if (participationNewStatus == ParticipationStatus.REJECTED) {
            for (Participation p : participationStatusPending) {
                p.setStatus(ParticipationStatus.REJECTED);
                participationsStatusRejected.add(p);
                log.debug("Заявка отклонена: requestId={}, eventId={}", p.getId(), eventId);
            }
        } else {
            log.warn("Недопустимый статус для обновления: status={}", participationNewStatus);
            throw new IllegalArgumentException("Данного статуса не существует");
        }

        // Обновление количества подтвержденных заявок в событии
        event.setConfirmedRequests((int) confirmedParticipation);
        eventRepository.save(event);
        participationRepository.saveAll(participationStatusPending);

        log.debug("Статусы заявок обновлены: eventId={}, confirmed={}, rejected={}, пропущено={}",
                eventId, participationsStatusConfirmed.size(), participationsStatusRejected.size(), participationList.size());

        return new ParticipationStatusResult(
                participationsStatusConfirmed.stream().map(ParticipationMapper::mapToParticipationDto).collect(Collectors.toList()),
                participationsStatusRejected.stream().map(ParticipationMapper::mapToParticipationDto).collect(Collectors.toList())
        );
    }

}
