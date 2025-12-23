package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.exceptions.UpdateValidation;
import ru.practicum.location.dto.LocationDto;
import ru.practicum.state.EventStateAction;

import java.time.LocalDateTime;

@Data
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000, message = "Краткое описание не может быть длиннее 2000 символов", groups = UpdateValidation.class)
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Полное описание события не может быть длиннее 7000 символов", groups = UpdateValidation.class)
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @FutureOrPresent(message = "Дата события должна быть в будущем", groups = UpdateValidation.class)
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    @Positive(message = "Лимит не может быть отрицательным", groups = UpdateValidation.class)
    private Integer participantLimit;

    private Boolean requestModeration;

    private EventStateAction stateAction;

    @Size(min = 3, max = 120, message = "Заголовок не может быть длиннее 120 символов", groups = UpdateValidation.class)
    private String title;

    public boolean hasTittle() {
        return !(title == null || title.isBlank());
    }

    public boolean hasAnnotation() {
        return !(annotation == null || annotation.isBlank());
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasEventDate() {
        return !(eventDate == null);
    }


    public boolean hasParticipantLimit() {
        return !(participantLimit == null);
    }

    public boolean hasCategory() {
        return !(category == null);
    }

    public boolean hasLocation() {
        return !(location == null);
    }

    public boolean hasPaid() {
        return !(paid == null);
    }

    public boolean hasRequestModeration() {
        return !(requestModeration == null);
    }

}
