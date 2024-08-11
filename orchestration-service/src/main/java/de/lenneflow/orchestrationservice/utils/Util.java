package de.lenneflow.orchestrationservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lenneflow.orchestrationservice.feignmodels.Function;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Util {

    public static Function deserializeFunction(byte[] serializedFunction) {
        ObjectMapper mapper = new ObjectMapper();
        Function function = null;
        try {
            function = mapper.readValue(serializedFunction, Function.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return function;
    }

    public static byte[] serializeFunction(Function function) {
        ObjectMapper mapper = new ObjectMapper();
        byte[] serializedFunction = null;
        try {
            serializedFunction = mapper.writeValueAsBytes(function);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return serializedFunction;
    }

}
