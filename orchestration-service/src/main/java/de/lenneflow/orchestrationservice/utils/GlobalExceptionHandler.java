package de.lenneflow.orchestrationservice.utils;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Object> handleBadParametersException(BadRequestException e) {
        // do what you want with e
        return new ResponseEntity<>("Bad parameters entered for the workflow", HttpStatus.OK);
    }
}
