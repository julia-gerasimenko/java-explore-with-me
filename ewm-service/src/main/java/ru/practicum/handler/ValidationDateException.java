package ru.practicum.handler;

public class ValidationDateException extends RuntimeException {
    public ValidationDateException(String message) {
        super(message);
    }
}
