package ru.practicum.category;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryRequest;
import ru.practicum.category.dto.UpdateCategoryRequest;
import ru.practicum.event.EventRepository;
import ru.practicum.exceptions.ConflictErrorException;
import ru.practicum.exceptions.NotFoundException;

import java.util.List;

import static ru.practicum.category.CategoryMapper.mapToCategory;
import static ru.practicum.category.CategoryMapper.mapToCategoryDto;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, EventRepository eventRepository) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            log.warn("Ошибка категория с таким именем уже существует");
            throw new ConflictErrorException("Данное имя категории уже занято");
        }
        log.debug("Начинается добавление категории по запросу {}", request);
        Category category = mapToCategory(request);
        log.debug("Запрос на добавление категории конвертирован в объект класса Category {}", category);
        category = categoryRepository.save(category);
        log.debug("Добавлена категория {}", category);
        return mapToCategoryDto(category);
    }

    @Override
    public List<CategoryDto> findAllCategory(Integer from, Integer size) {
        log.info("Начало поиска категорий: from={}, size={}", from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable)
                .stream()
                .map(CategoryMapper::mapToCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto getCategoryId(Long catId) {
        return categoryRepository.findById(catId)
                .map(CategoryMapper::mapToCategoryDto)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске категории. Категория с id={} не найдена", catId);
                    return new NotFoundException("Категория с id=" + catId + " не найдена");
                });
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, UpdateCategoryRequest request) {
        log.info("Запрос на обновление категории: catId={}, newName='{}'", catId, request.getName());

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> {
                    log.warn("Ошибка при поиске категории. Категория с id={} не найдена", catId);
                    return new NotFoundException("Категория с id=" + catId + " не найдена");
                });

        // Проверка на изменилось ли название категории
        String oldName = category.getName();
        String newName = request.getName();

        if (newName != null && newName.equals(oldName)) {
            log.debug("Имя категории не изменилось: catId={}, name='{}'", catId, oldName);
            return mapToCategoryDto(category);
        }

        // Название категории изменилось - проверка на уникальность
        if (newName != null && !newName.equals(oldName)) {
            boolean categoryWithSameNameExists = categoryRepository.existsByNameIgnoreCaseAndIdNot(newName, catId);
            if (categoryWithSameNameExists) {
                log.warn("Категория с таким именем уже существует: name='{}', catId={}", newName, catId);
                throw new ConflictErrorException("Категория с названием '" + newName + "' уже существует");
            }
        }

        Category updatedCategory = CategoryMapper.updateCategoryFields(category, request);
        log.debug("Поля категории обновлены: catId={}", catId);

        if (!category.equals(updatedCategory)) {
            updatedCategory = categoryRepository.save(updatedCategory);
            log.debug("Категория обновлена: catId={}, newName='{}'", catId, updatedCategory.getName());
        } else {
            log.debug("Категория не требует сохранения: catId={}", catId);
        }

        return mapToCategoryDto(updatedCategory);
    }

    @Override
    public void deleteCategory(Long catId) {

        if (!eventRepository.findByCategoryId(catId).isEmpty()) {
            throw new ConflictErrorException("Запрещено удалять категорию с привязанными событиями");
        }

        categoryRepository.deleteById(catId);
        log.info("Категория {} успешна удалена.", catId);
    }
}
