package ru.practicum.category;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryRequest;
import ru.practicum.category.dto.UpdateCategoryRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryMapper {

    public static Category mapToCategory(NewCategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());

        return category;
    }

    public static CategoryDto mapToCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());

        return dto;
    }

    public static Category updateCategoryFields(Category category, UpdateCategoryRequest request) {
        if (request.hasName()) {
            category.setName(request.getName());
        }

        return category;
    }
}
