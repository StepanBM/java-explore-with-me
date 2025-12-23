package ru.practicum.state;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EventState {
    PENDING,
    PUBLISHED,
    CANCELED;

    @JsonValue
    public String getName() {
        return name();
    }
}
