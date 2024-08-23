package de.lenneflow.orchestrationservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.lenneflow.orchestrationservice.feignmodels.Function;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;


public class Util {

    public static Function deserializeFunction(byte[] serializedFunction) {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        Function function = null;
        try {
            function = mapper.readValue(serializedFunction, Function.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return function;
    }

    public static byte[] serializeFunction(Function function) {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        byte[] serializedFunction = null;
        try {
            serializedFunction = mapper.writeValueAsBytes(function);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return serializedFunction;
    }

    public static String getFunctionEndpointUrl(Function function) {
        String url = StringUtils.removeEnd(function.getEndPointRoot(), "/") + "/" + StringUtils.removeStart(function.getEndPointPath(), "/");
        //TODO
        return "https://lenneflowworker/api/functionjava/process";
    }

}
