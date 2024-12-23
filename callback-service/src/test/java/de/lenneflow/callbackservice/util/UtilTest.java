package de.lenneflow.callbackservice.util;

import de.lenneflow.callbackservice.dto.ResultQueueElement;
import de.lenneflow.callbackservice.enums.RunStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void serializeResultQueueElement_shouldSerializeValidResultQueueElement() {
        ResultQueueElement resultQueueElement = new ResultQueueElement();
        resultQueueElement.setStepInstanceId("123");
        resultQueueElement.setRunStatus(RunStatus.COMPLETED);

        byte[] serializedFunction = Util.serializeResultQueueElement(resultQueueElement);

        assertNotNull(serializedFunction);
        assertTrue(serializedFunction.length > 0);
    }

    @Test
    void deserializeFunction_shouldDeserializeValidSerializedResultQueueElement() {
        ResultQueueElement resultQueueElement = new ResultQueueElement();
        resultQueueElement.setStepInstanceId("123");
        resultQueueElement.setRunStatus(RunStatus.COMPLETED);
        byte[] serializedFunction = Util.serializeResultQueueElement(resultQueueElement);

        ResultQueueElement deserializedFunction = Util.deserializeResultQueueElement(serializedFunction);

        assertNotNull(deserializedFunction);
        assertEquals("123", deserializedFunction.getStepInstanceId());
        assertEquals(RunStatus.COMPLETED, deserializedFunction.getRunStatus());
    }

    @Test
    void deserializeResultQueueElement_shouldThrowExceptionWhenSerializedResultQueueElementIsInvalid() {
        byte[] invalidSerializedFunction = "invalid data".getBytes();

        assertThrows(RuntimeException.class, () -> Util.deserializeResultQueueElement(invalidSerializedFunction));
    }

    @Test
    void deserializeResultQueueElement_shouldThrowExceptionWhenSerializedResultQueueElementIsNull() {
        assertThrows(RuntimeException.class, () -> Util.deserializeResultQueueElement(null));
    }
}