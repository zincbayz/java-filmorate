package ru.yandex.practicum.filmorate.exception_handler.exceptions;

public class EntityAllreadyExistExeption extends Exception {
    public EntityAllreadyExistExeption (String message) {
        super(message);
    }
}
