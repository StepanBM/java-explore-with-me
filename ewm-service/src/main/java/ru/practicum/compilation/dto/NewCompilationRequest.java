package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.exceptions.CreateValidation;

import java.util.List;

@Data
public class NewCompilationRequest {

    // Заголовок подборки
    @NotBlank(message = "Заголовок подборки не может быть пустым", groups = CreateValidation.class)
    @Size(min = 1, max = 50, message = "Заголовок подборки должно содержать от 1 до 50 символов", groups = CreateValidation.class)
    private String title;

    // Закреплена ли подборка на главной странице сайта
    private Boolean pinned = false;

    // Список идентификаторов событий входящих в подборку
    List<Long> events;
}
