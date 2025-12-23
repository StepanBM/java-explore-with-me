package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.practicum.exceptions.CreateValidation;
import ru.practicum.location.dto.LocationDto;

import java.time.LocalDateTime;

@Data
public class NewEventRequest {

    @NotBlank(message = "Краткое описание не может быть пустым", groups = CreateValidation.class)
    @Size(min = 20, max = 2000, message = "Краткое описание должно содержать от 20 до 2000 символов", groups = CreateValidation.class)
    private String annotation;

    @NotNull(message = "Категория не может быть пустой", groups = CreateValidation.class)
    private Long category;

    @NotBlank(message = "Полное описание события не может быть пустым", groups = CreateValidation.class)
    @Size(min = 20, max = 7000, message = "Полное описание должно содержать от 20 до 7000 символов", groups = CreateValidation.class)
    private String description;

    @NotNull(message = "Дата и время на которые намечено событие не может быть пустым", groups = CreateValidation.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Локация не может быть пустой", groups = CreateValidation.class)
    private LocationDto location;

    private boolean paid = false;

    @PositiveOrZero(message = "Лимит не может быть отрицательным", groups = CreateValidation.class)
    private int participantLimit;

    private Boolean requestModeration = true;

    @NotBlank(message = "Заголовок не может быть пустым", groups = CreateValidation.class)
    @Size(min = 3, max = 120, message = "Заголовок должен содержать от 3 до 120 символов", groups = CreateValidation.class)
    private String title;

}
