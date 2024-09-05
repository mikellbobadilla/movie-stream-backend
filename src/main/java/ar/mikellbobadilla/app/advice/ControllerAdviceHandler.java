package ar.mikellbobadilla.app.advice;

import ar.mikellbobadilla.app.exceptions.ResourceException;
import ar.mikellbobadilla.app.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ControllerAdviceHandler {

    @ExceptionHandler(ResourceException.class)
    @ResponseStatus(BAD_REQUEST)
    ErrorResponse resourceHandler(ResourceException exc) {
        return responseBuilder(BAD_REQUEST, exc.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    ErrorResponse resourceNotFoundHandler(ResourceNotFoundException exc) {
        return responseBuilder(NOT_FOUND, exc.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    ErrorResponse runtimeExcHandler(RuntimeException exc) {
        exc.printStackTrace();
        return responseBuilder(INTERNAL_SERVER_ERROR, "Server error");
    }

    private ErrorResponse responseBuilder(HttpStatus status, String message) {
        return new ErrorResponse(status.value(), message);
    }
}
