package de.lenneflow.orchestrationservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lenneflow.orchestrationservice.dto.QueueElement;
import de.lenneflow.orchestrationservice.dto.ResultQueueElement;
import de.lenneflow.orchestrationservice.dto.RunNotification;

import java.io.IOException;

/**
 * Utility class
 *
 * @author Idrissa Ganemtore
 */
public class Util {

    private Util() {
    }

    /**
     * Deserializes a byte array and returns a function dto object.
     *
     * @param serializedFunctionDto the byte array
     * @return the {@link QueueElement} object
     */
    public static QueueElement deserializeQueueElement(byte[] serializedFunctionDto) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        QueueElement queueElement;
        queueElement = mapper.readValue(serializedFunctionDto, QueueElement.class);
        return queueElement;
    }

    /**
     * Deserializes a byte array and returns a function dto object.
     *
     * @param serialized the byte array
     * @return the {@link QueueElement} object
     */
    public static ResultQueueElement deserializeResultQueueElement(byte[] serialized) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ResultQueueElement queueElement;
        queueElement = mapper.readValue(serialized, ResultQueueElement.class);
        return queueElement;
    }

    /**
     * Serializes a function dto object to a byte array.
     *
     * @param notification the object to serialize
     * @return the byte array
     */
    public static byte[] serialize(RunNotification notification) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        byte[] serializedFunction;
        serializedFunction = mapper.writeValueAsBytes(notification);
        return serializedFunction;
    }

    /**
     * Serializes a function dto object to a byte array.
     *
     * @param queueElement the object to serialize
     * @return the byte array
     */
    public static byte[] serialize(QueueElement queueElement) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        byte[] serializedFunction;
        serializedFunction = mapper.writeValueAsBytes(queueElement);
        return serializedFunction;
    }

    /**
     * Serializes a function dto object to a byte array.
     *
     * @param queueElement the object to serialize
     * @return the byte array
     */
    public static byte[] serialize(ResultQueueElement queueElement) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        byte[] serializedFunction;
        serializedFunction = mapper.writeValueAsBytes(queueElement);
        return serializedFunction;
    }


    public static void pause(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
