package de.lenneflow.callbackservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lenneflow.callbackservice.dto.ResultQueueElement;

import java.io.IOException;


public class Util {

    public static ResultQueueElement deserializeResultQueueElement(byte[] serializedResultQueueElement) {
        ObjectMapper mapper = new ObjectMapper();
        ResultQueueElement resultQueueElement = null;
        try {
            resultQueueElement = mapper.readValue(serializedResultQueueElement, ResultQueueElement.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resultQueueElement;
    }

    public static byte[] serializeResultQueueElement(ResultQueueElement resultQueueElement) {
        ObjectMapper mapper = new ObjectMapper();
        byte[] serializedFunction = null;
        try {
            serializedFunction = mapper.writeValueAsBytes(resultQueueElement);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return serializedFunction;
    }

}
