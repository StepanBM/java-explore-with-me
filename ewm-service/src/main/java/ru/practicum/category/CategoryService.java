package ru.practicum.category;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryRequest;
import ru.practicum.category.dto.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {

    CategoryDto addCategory(NewCategoryRequest request);

    List<CategoryDto> findAllCategory(Integer from, Integer size);

    CategoryDto getCategoryId(Long catId);

    CategoryDto updateCategory(Long catId, UpdateCategoryRequest request);

    void deleteCategory(Long catId);

}
