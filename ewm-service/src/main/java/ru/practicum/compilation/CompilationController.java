package ru.practicum.compilation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationRequest;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.exceptions.CreateValidation;
import ru.practicum.exceptions.UpdateValidation;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping()
public class CompilationController {

    private final CompilationService compilationService;

    public CompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@Validated(CreateValidation.class) @RequestBody NewCompilationRequest request) {
        log.info("Запрос на создание новой подборки");
        return compilationService.addCompilation(request);
    }

    @PatchMapping("/admin/compilations/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId, @Validated(UpdateValidation.class) @RequestBody UpdateCompilationRequest request) {
        log.info("Запрос на обновление подборки: id={}, данные={}", compId, request);
        return compilationService.updateCompilation(compId, request);
    }

    @GetMapping("/compilations")
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(required = false, defaultValue = "0") int from,
                                                @RequestParam(required = false, defaultValue = "10") int size) {
        log.info("Запрос на получение подборок: pinned={}, from={}, size={}", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilationId(@PathVariable Long compId) {
        log.info("Запрос на получение подборки по id: compId={}", compId);
        return compilationService.getCompilationId(compId);
    }

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilationId(@PathVariable Long compId) {
        log.info("Запрос на удаление подборки: compId={}", compId);
        compilationService.deleteCompilationId(compId);
    }

}
