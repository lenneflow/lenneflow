package de.lenneflow.callbackservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lenneflow.callbackservice.dto.FunctionDTO;

import java.io.IOException;


public class Util {

    public static FunctionDTO deserializeFunction(byte[] serializedFunctionDto) {
        ObjectMapper mapper = new ObjectMapper();
        FunctionDTO functionDto = null;
        try {
            functionDto = mapper.readValue(serializedFunctionDto, FunctionDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return functionDto;
    }

    public static byte[] serializeFunctionDto(FunctionDTO functionDto) {
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
