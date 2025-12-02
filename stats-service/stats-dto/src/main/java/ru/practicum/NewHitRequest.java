package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.practicum.validation.CreateValidation;

import java.time.LocalDateTime;

@Data
public class NewHitRequest {

    @NotBlank(message = "Название приложения не может быть пустым", groups = CreateValidation.class)
    @Length(max = 255, message = "Название приложения не может привышать длину в 100 символов", groups = CreateValidation.class)
    private String app;

    @NotBlank(message = "Url не может быть пустым", groups = CreateValidation.class)
    @Length(max = 255, message = "Uri не может привышать длину в 100 символов", groups = CreateValidation.class)
    private String uri;

    @NotBlank(message = "Ip адрес не может быть пустым", groups = CreateValidation.class)
    @Length(max = 255, message = "Ip адрес не может привышать длину в 100 символов", groups = CreateValidation.class)
    private String ip;

    @NotNull(message = "Дата и время, когда был совершен запрос не может быть null", groups = CreateValidation.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

}
