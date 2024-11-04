package de.lenneflow.callbackservice.util;

import de.lenneflow.callbackservice.dto.FunctionPayload;
import de.lenneflow.callbackservice.exception.PayloadNotValidException;

public class Validator {
    public static void validate(FunctionPayload payload) {
        if(payload == null) {
            throw new PayloadNotValidException("Payload is null");
        }
        if(payload.getOutputData() == null || payload.getOutputData().isEmpty()) {
            throw new PayloadNotValidException("Output data is null or empty");
        }
        if (payload.getRunStatus() == null){
            throw new PayloadNotValidException("Run Status is null");
        }
    }
}
