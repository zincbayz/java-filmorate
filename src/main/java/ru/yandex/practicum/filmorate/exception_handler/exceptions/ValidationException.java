package ru.yandex.practicum.filmorate.exception_handler.exceptions;

public class ValidationException extends RuntimeException {
    public ValidationException (String message) {
        super(message);
    }
}
