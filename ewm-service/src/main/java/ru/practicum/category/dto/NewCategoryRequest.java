package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.exceptions.CreateValidation;

@Data
public class NewCategoryRequest {

    @NotBlank(message = "Название катагории не может быть пустым", groups = CreateValidation.class)
    @Size(min = 1, max = 50, message = "Название катагории не может быть длиннее 50 символов", groups = CreateValidation.class)
    private String name;
}
