package de.lenneflow.orchestrationservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.lenneflow.orchestrationservice.dto.QueueElement;
import de.lenneflow.orchestrationservice.dto.ResultQueueElement;
import de.lenneflow.orchestrationservice.dto.RunNotification;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void deserializeQueueElement_returnsQueueElement() throws IOException {
        byte[] serialized = "{\"callBackUrl\":\"someValue\"}".getBytes();
        QueueElement result = Util.deserializeQueueElement(serialized);
        assertNotNull(result);
        assertEquals("someValue", result.getCallBackUrl());
    }

    @Test
    void deserializeQueueElement_throwsIOExceptionForInvalidData() {
        byte[] serialized = "invalid data".getBytes();
        assertThrows(IOException.class, () -> Util.deserializeQueueElement(serialized));
    }

    @Test
    void deserializeResultQueueElement_returnsResultQueueElement() throws IOException {
        byte[] serialized = "{\"stepInstanceId\":\"someValue\"}".getBytes();
        ResultQueueElement result = Util.deserializeResultQueueElement(serialized);
        assertNotNull(result);
        assertEquals("someValue", result.getStepInstanceId());
    }

    @Test
    void deserializeResultQueueElement_throwsIOExceptionForInvalidData() {
        byte[] serialized = "invalid data".getBytes();
        assertThrows(IOException.class, () -> Util.deserializeResultQueueElement(serialized));
    }

    @Test
    void serializeRunNotification_returnsByteArray() throws JsonProcessingException {
        RunNotification notification = new RunNotification();
        notification.setWorkflowStepInstanceUid("someValue");
        byte[] result = Util.serialize(notification);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void serializeQueueElement_returnsByteArray() throws JsonProcessingException {
        QueueElement queueElement = new QueueElement();
        queueElement.setFunctionType("someValue");
        byte[] result = Util.serialize(queueElement);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void serializeResultQueueElement_returnsByteArray() throws JsonProcessingException {
        ResultQueueElement queueElement = new ResultQueueElement();
        queueElement.setFailureReason("someValue");
        byte[] result = Util.serialize(queueElement);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void pause_doesNotThrowException() {
        assertDoesNotThrow(() -> Util.pause(100));
    }
}