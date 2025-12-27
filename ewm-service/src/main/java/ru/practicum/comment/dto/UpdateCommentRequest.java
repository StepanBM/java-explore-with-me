package ru.practicum.comment.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.practicum.exceptions.UpdateValidation;

@Data
public class UpdateCommentRequest {

    @Length(min = 2, max = 2000, message = "Комментарий должен содержать от 2 до 2000 символов", groups = UpdateValidation.class)
    private String text;

    public boolean hasText() {
        return !(text == null || text.isBlank());
    }
}
