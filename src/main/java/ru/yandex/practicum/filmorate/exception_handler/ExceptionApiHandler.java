package ru.yandex.practicum.filmorate.exception_handler;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception_handler.exeptions.*;

@Slf4j
@RestControllerAdvice
public class ExceptionApiHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ValidationException handleValid(MethodArgumentNotValidException e) {
        log.error("Validation Error");
        return(new ValidationException("Validation Error " + e.getMessage()));
    }

    @ExceptionHandler({RequiredObjectWasNotFound.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody RequiredObjectWasNotFound handleNotFound(RequiredObjectWasNotFound e) {
        log.error("Required object wasn't found: " + e.getMessage());
        return(e);
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody InternalServerError handleNotFound(InternalServerError e) {
        log.error("Exception has occurred " + e.getMessage());
        return(e);
    }

    @ExceptionHandler({DirectorNotFound.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody DirectorNotFound handleNotFound(DirectorNotFound e) {
        log.error("Director wasn't found: " + e.getMessage());
        return(e);
    }


    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ErrorMessage> InvalidParameterException(InvalidParameterException exception) {
        log.error(exception.getMessage(), exception);
       return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorMessage(exception.getMessage()));
    }

    @ExceptionHandler(EntityAllreadyExistExeption.class)
    public ResponseEntity<ErrorMessage> EntityAllreadyExistExeption(EntityAllreadyExistExeption exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorMessage(exception.getMessage()));
    }


    @ExceptionHandler(EntityNotFoundExeption.class)
    public ResponseEntity<ErrorMessage> EntityNotFoundExeption(EntityNotFoundExeption exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorMessage(exception.getMessage()));
    }


}
