package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.practicum.exceptions.CreateValidation;

@Data
public class NewCategoryRequest {

    @NotBlank(message = "Название катагории не может быть пустым", groups = CreateValidation.class)
    @Length(min = 1, max = 50, message = "Название катагории не может быть длиннее 50 символов", groups = CreateValidation.class)
    private String name;
}
