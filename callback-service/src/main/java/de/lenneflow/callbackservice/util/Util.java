package de.lenneflow.callbackservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lenneflow.callbackservice.dto.ResultQueueElement;

import java.io.IOException;


public class Util {

    public static ResultQueueElement deserializeFunction(byte[] serializedFunctionDto) {
        ObjectMapper mapper = new ObjectMapper();
        ResultQueueElement functionDto = null;
        try {
            functionDto = mapper.readValue(serializedFunctionDto, ResultQueueElement.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return functionDto;
    }

    public static byte[] serializeFunctionDto(ResultQueueElement functionDto) {
        ObjectMapper mapper = new ObjectMapper();
        byte[] serializedFunction = null;
        try {
            serializedFunction = mapper.writeValueAsBytes(functionDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return serializedFunction;
    }

}
