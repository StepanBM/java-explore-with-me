package ru.practicum.category;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryRequest;
import ru.practicum.category.dto.UpdateCategoryRequest;
import ru.practicum.exceptions.CreateValidation;
import ru.practicum.exceptions.UpdateValidation;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Validated(CreateValidation.class) @RequestBody NewCategoryRequest request) {
        log.info("Запрос администратора на создание новой категории: название='{}'", request.getName());
        return categoryService.addCategory(request);
    }

    @GetMapping("/categories")
    public List<CategoryDto> findAllCategory(@RequestParam(name = "from", defaultValue = "0") Integer from,
                                      @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Запрос на получение всех категорий: from={}, size={}", from, size);
        return categoryService.findAllCategory(from, size);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getCategoryId(@PathVariable("catId") Long catId) {
        log.info("Запрос на получение категории по id: catId={}", catId);
        return categoryService.getCategoryId(catId);
    }

    @PatchMapping("/admin/categories/{catId}")
    public CategoryDto updateCategory(@PathVariable("catId") Long catId, @Validated(UpdateValidation.class) @RequestBody UpdateCategoryRequest request) {
        log.info("Запрос администратора на обновление категории: id={}", catId);
        return categoryService.updateCategory(catId, request);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable("catId") Long catId) {
        log.info("Запрос от администратора на удаление категории: catId={}", catId);
        categoryService.deleteCategory(catId);
    }
}
