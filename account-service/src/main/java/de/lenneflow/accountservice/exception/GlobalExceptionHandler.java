package de.lenneflow.accountservice.exception;

import de.lenneflow.functionservice.exception.InternalServiceException;
import de.lenneflow.functionservice.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * This is the global exception handler for the function service. It is annotated as controller advise
 * and send responses with error messages to the rest calls.
 *
 * @author Idrissa Ganemtore
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PayloadNotValidException.class)
    public ResponseEntity<String> handlePayloadNotValidException(PayloadNotValidException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InternalServiceException.class)
    public ResponseEntity<String> handleInternalServiceException(Exception ex) {
        return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}