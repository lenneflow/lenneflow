package de.lenneflow.functionservice.exception;

import java.io.Serial;

/**
 * If the payload of a rest call is not valid, this exception will be thrown.
 *
 * @author Idrissa Ganemtore
 */
public class PayloadNotValidException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PayloadNotValidException(String msg) {
        super(msg);
    }
}
