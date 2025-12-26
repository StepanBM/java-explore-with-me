package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.practicum.exceptions.CreateValidation;

@Data
public class NewCommentRequest {

    @NotBlank(message = "Комментарий не может быть пустым", groups = CreateValidation.class)
    @Length(min = 2, max = 2000, message = "Комментарий должен содержать от 2 до 2000 символов", groups = CreateValidation.class)
    private String text;

}
