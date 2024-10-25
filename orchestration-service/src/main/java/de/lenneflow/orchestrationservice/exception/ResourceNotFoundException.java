package de.lenneflow.orchestrationservice.exception;

import java.io.Serial;

/**
 * Sometimes, resources are necessary to process. If a such resource is not found, this exception will be thrown
 *
 * @author Idrissa Ganemtore
 */
public class ResourceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    public ResourceNotFoundException(String msg) {
        super(msg);
    }

}
