package de.lenneflow.orchestrationservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lenneflow.orchestrationservice.feignmodels.Function;

import java.io.IOException;

public class Util {

    public static Function deserializeFunction(byte[] serializedFunction) {
        ObjectMapper mapper = new ObjectMapper();
        Function functionResult = null;
        try {
            functionResult = mapper.readValue(serializedFunction, Function.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return functionResult;
    }

    public static String serializeFunction(Function function) {
        ObjectMapper mapper = new ObjectMapper();
        String serializedFunction = null;
        try {
            serializedFunction = mapper.writeValueAsString(function);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return serializedFunction;
    }


}
