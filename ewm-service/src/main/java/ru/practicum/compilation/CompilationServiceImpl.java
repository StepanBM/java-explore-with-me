package ru.practicum.compilation;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationRequest;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.compilation.CompilationMapper.mapToCompilation;
import static ru.practicum.compilation.CompilationMapper.mapToCompilationDto;

@Slf4j
@Service
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Autowired
    public CompilationServiceImpl(CompilationRepository compilationRepository,
                                  EventRepository eventRepository) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationRequest request) {
        log.info("Начало добавления новой подборки: request={}", request);

        List<Event> events = new ArrayList<>();

        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            events.addAll(eventRepository.findAllById(request.getEvents()));
            log.debug("Найдено {} событий для включения в подборку", events.size());
        }

        Compilation compilation = mapToCompilation(request, events);
        log.debug("Создается объект Compilation: {}", compilation);

        Compilation savedCompilation = compilationRepository.save(compilation);

        return mapToCompilationDto(savedCompilation);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        log.info("Начало обновления подборки: compId={}, request={}", compId, request);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске подборки. Подборка с id={} не найден", compId);
                    return new EntityNotFoundException("Подборка id=" + compId + " не найдена");
                });

        if (request.getEvents() != null) {
            List<Event> newEvents = new ArrayList<>(eventRepository.findAllById(request.getEvents()));
            compilation.setEvents(newEvents);
            log.debug("Обновлен список событий, всего {} элементов", newEvents.size());
        }

        return mapToCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public CompilationDto getCompilationId(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске подборки. Подборка с id={} не найден", compId);
                    return new EntityNotFoundException("Подборка id=" + compId + " не найдена");
                });

        return mapToCompilationDto(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        log.info("Получение подборок: pinned={}, from={}, size={}", pinned, from, size);

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        Page<Compilation> compilations;

        if (pinned == null) {
            log.debug("Запрос всех подборок без фильтра");
            compilations = compilationRepository.findAll(pageable);
        } else {
            log.info("Запрос подборок с pinned={}", pinned);
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        }

        return compilations.stream()
                .map(CompilationMapper::mapToCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCompilationId(Long comId) {
        compilationRepository.deleteById(comId);
        log.info("Подборка {} успешно удалена.", comId);
    }

}
