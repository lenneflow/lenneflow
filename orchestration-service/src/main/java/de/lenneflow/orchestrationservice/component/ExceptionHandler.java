package de.lenneflow.orchestrationservice.component;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<Object> handleBadParametersException(BadRequestException e) {
        // do what you want with e
        return new ResponseEntity<>("Bad parameters entered for the workflow", HttpStatus.OK);
    }
}