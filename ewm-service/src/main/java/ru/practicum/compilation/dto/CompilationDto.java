package ru.practicum.compilation.dto;

import lombok.Data;
import ru.practicum.event.dto.EventDto;

import java.util.List;

@Data
public class CompilationDto {

    private Long id;

    // Заголовок подборки
    private String title;

    // Закреплена ли подборка на главной странице сайта
    private boolean pinned;

    // Список событий входящих в подборку
    private List<EventDto> events;
}
