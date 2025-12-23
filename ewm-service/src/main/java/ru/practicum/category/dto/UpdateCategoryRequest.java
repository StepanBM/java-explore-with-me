package ru.practicum.category.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.exceptions.UpdateValidation;

@Data
public class UpdateCategoryRequest {

    @Size(min = 1, max = 50, message = "Название катагории не может быть длиннее 50 символов", groups = UpdateValidation.class)
    private String name;

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }
}
