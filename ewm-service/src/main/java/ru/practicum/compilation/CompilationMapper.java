package ru.practicum.compilation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationRequest;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.event.Event;
import ru.practicum.event.EventMapper;
import ru.practicum.event.dto.EventDto;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompilationMapper {

    public static Compilation mapToCompilation(NewCompilationRequest request, List<Event> events) {
        Compilation compilation = new Compilation();
        compilation.setTitle(request.getTitle());
        compilation.setPinned(request.getPinned());
        compilation.setEvents(events);

        return compilation;
    }

    public static CompilationDto mapToCompilationDto(Compilation compilation) {
        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setTitle(compilation.getTitle());
        dto.setPinned(compilation.getPinned());

        List<EventDto> eventDto = compilation.getEvents().stream()
                .map(EventMapper::mapToEventDto)
                .collect(Collectors.toList());
        dto.setEvents(eventDto);

        return dto;
    }

    public static Compilation updateCompilationFields(Compilation compilation, UpdateCompilationRequest request) {

        if (request.hasTittle()) {
            compilation.setTitle(request.getTitle());
        }
        if (request.hasPinned()) {
            compilation.setPinned(request.getPinned());
        }

        return compilation;
    }
}
