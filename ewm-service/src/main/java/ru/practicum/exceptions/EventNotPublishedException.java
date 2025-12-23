package ru.practicum.exceptions;

public class EventNotPublishedException extends RuntimeException {
    public EventNotPublishedException(String message) {
        super(message);
    }
}
