package ru.yandex.practicum.filmorate.exception_handler.exeptions;

public class EntityAllreadyExistExeption extends Exception{
    public EntityAllreadyExistExeption (String message) {
        super(message);
    }
}
