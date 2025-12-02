package ru.practicum.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class, ConstraintViolationException.class,
            HttpMessageNotReadableException.class, org.springframework.web.bind.MissingRequestHeaderException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(final Exception e) {
         log.debug("Ошибка валидации. {}", e.getMessage());
        if (e.getClass() == ValidationException.class) {
            return new ErrorResponse(
                    "Некорректное значение параметра ", e.getMessage()
            );
        } else if (e.getClass() == HttpMessageNotReadableException.class) {
            log.debug("Ошибка, тело запроса отсутствует. {}", e.getMessage());
            return new ErrorResponse("Некорректный запрос", "Тело запроса отсутствует");
        } else if (e.getClass() == MissingRequestHeaderException.class) {
            log.debug("Ошибка, заголовок отсутствует. {}", e.getMessage());
            return new ErrorResponse("Некорректный запрос", "Заголовок отсутствует");
        } else if (e.getClass() == ConstraintViolationException.class) {
            log.debug("Ошибка, некорректное значение. {}", e.getMessage());
            return new ErrorResponse("Некорректное значение", "Ошибка валидации");
        } else {
            return new ErrorResponse(
                    "Некорректное значение параметра " + ((MethodArgumentNotValidException) e).getParameter(),
                    e.getMessage()
            );
        }
    }
}
