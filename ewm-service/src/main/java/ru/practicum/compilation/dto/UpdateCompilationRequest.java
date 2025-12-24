package ru.practicum.compilation.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.practicum.exceptions.UpdateValidation;

import java.util.List;

@Data
public class UpdateCompilationRequest {

    // Заголовок подборки
    @Length(min = 1, max = 50, message = "Заголовок подборки должно содержать от 1 до 50 символов", groups = UpdateValidation.class)
    private String title;

    // Закреплена ли подборка на главной странице сайта
    private Boolean pinned;

    // Список идентификаторов событий входящих в подборку
    private List<Long> events;

    public boolean hasTittle() {
        return !(title == null || title.isBlank());
    }

    public boolean hasPinned() {
        return !(pinned == null);
    }
}
