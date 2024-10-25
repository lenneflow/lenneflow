package de.lenneflow.orchestrationservice.exception;

import java.io.Serial;

/**
 * Exception that is thrown when an internal error happens during the process of a rest call
 *
 * @author Idrissa Ganemtore
 */
public class InternalServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InternalServiceException(String msg) {
        super(msg);
    }
}
