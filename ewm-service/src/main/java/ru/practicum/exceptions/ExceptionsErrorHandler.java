package ru.practicum.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionsErrorHandler {

    @ExceptionHandler({NotFoundException.class, EventNotPublishedException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(Exception e) {
        log.debug("Ошибка, объект не найден. {}", e.getMessage());
        if (e.getClass() == NotFoundException.class) {
            return new ErrorResponse("Ошибка, объект не найден", "Данного объекта нет");
        } else if (e.getClass() == EventNotPublishedException.class) {
            return new ErrorResponse(
                    "Не опубликовано",
                    e.getMessage()
            );
        }
        return new ErrorResponse("Ошибка", "Произошла неизвестная ошибка");
    }

    @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class, ConstraintViolationException.class,
            HttpMessageNotReadableException.class, MissingRequestHeaderException.class, ErrorEventDateException.class})
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
        } else if (e.getClass() == ErrorEventDateException.class) {
            log.debug("Ошибка, в дате события . {}", e.getMessage());
            return new ErrorResponse("Некорректное значение даты", "Ошибка события");
        } else {
            return new ErrorResponse(
                    "Некорректное значение параметра " + ((MethodArgumentNotValidException) e).getParameter(),
                    e.getMessage()
            );
        }
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicate(final ConflictErrorException e) {
        return new ErrorResponse(
                "Ошибка, объект с такими данными уже существует",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleServerError(final Exception e) {
        return new ErrorResponse(
                "Ошибка",
                e.getMessage()
        );
    }
}
