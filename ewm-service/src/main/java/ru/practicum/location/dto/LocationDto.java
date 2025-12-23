package ru.practicum.location.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationDto {

    @NotNull(message = "Широта не должна быть null")
    private Float lat;

    @NotNull(message = "Долгота не должна быть null")
    private Float lon;
}
