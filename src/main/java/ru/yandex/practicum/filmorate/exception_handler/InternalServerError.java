package ru.yandex.practicum.filmorate.exception_handler;

public class InternalServerError extends RuntimeException{
    public InternalServerError (String message) {
        super(message);
    }
}