package de.lenneflow.callbackservice.util;

import de.lenneflow.callbackservice.dto.FunctionPayload;
import de.lenneflow.callbackservice.enums.RunStatus;
import de.lenneflow.callbackservice.exception.PayloadNotValidException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    @Test
    void validate_shouldThrowExceptionWhenPayloadIsNull() {
        assertThrows(PayloadNotValidException.class, () -> Validator.validate(null));
    }

    @Test
    void validate_shouldThrowExceptionWhenOutputDataIsNull() {
        FunctionPayload payload = new FunctionPayload();
        payload.setOutputData(null);
        payload.setRunStatus(RunStatus.COMPLETED);

        assertThrows(PayloadNotValidException.class, () -> Validator.validate(payload));
    }

    @Test
    void validate_shouldThrowExceptionWhenOutputDataIsEmpty() {
        FunctionPayload payload = new FunctionPayload();
        payload.setOutputData(new HashMap<>());
        payload.setRunStatus(RunStatus.COMPLETED);

        assertThrows(PayloadNotValidException.class, () -> Validator.validate(payload));
    }

    @Test
    void validate_shouldThrowExceptionWhenRunStatusIsNull() {
        FunctionPayload payload = new FunctionPayload();
        payload.setOutputData(Map.of("key", "value"));
        payload.setRunStatus(null);

        assertThrows(PayloadNotValidException.class, () -> Validator.validate(payload));
    }

    @Test
    void validate_shouldPassWhenPayloadIsValid() {
        FunctionPayload payload = new FunctionPayload();
        payload.setOutputData(Map.of("key", "value"));
        payload.setRunStatus(RunStatus.COMPLETED);

        assertDoesNotThrow(() -> Validator.validate(payload));
    }
}