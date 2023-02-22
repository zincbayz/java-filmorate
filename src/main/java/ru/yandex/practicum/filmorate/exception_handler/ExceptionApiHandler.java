package ru.yandex.practicum.filmorate.exception_handler;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception_handler.exceptions.*;
import org.springframework.http.ResponseEntity;

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
    public ResponseEntity<ErrorMessage> handleNotFound(RequiredObjectWasNotFound e) {
        log.error("Required object wasn't found: " + e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorMessage> handleNotFound(InternalServerError e) {
        log.error("Exception has occurred " + e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorMessage(e.getMessage()));    }

    @ExceptionHandler({DirectorNotFound.class})
    public ResponseEntity<ErrorMessage> DirectorNotFound(DirectorNotFound e) {
        log.error("Director wasn't found: " + e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorMessage(e.getMessage()));
    }
        
    @ExceptionHandler({ReviewNotFound.class})
    public ResponseEntity<ErrorMessage> ReviewNotFound(ReviewNotFound e) {
        log.error("Review wasn't found " + e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorMessage(e.getMessage()));
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
