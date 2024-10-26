package de.lenneflow.orchestrationservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import de.lenneflow.orchestrationservice.dto.FunctionDto;
import de.lenneflow.orchestrationservice.enums.JsonSchemaVersion;
import de.lenneflow.orchestrationservice.exception.InternalServiceException;
import de.lenneflow.orchestrationservice.feignmodels.Function;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

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
     * @return the {@link FunctionDto} object
     */
    public static FunctionDto deserializeFunction(byte[] serializedFunctionDto) {
        ObjectMapper mapper = new ObjectMapper();
        FunctionDto functionDto = null;
        try {
            functionDto = mapper.readValue(serializedFunctionDto, FunctionDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return functionDto;
    }

    /**
     * Serializes a function dto object to a byte array.
     *
     * @param functionDto the object to serialize
     * @return the byte array
     */
    public static byte[] serializeFunctionDto(FunctionDto functionDto) {
        ObjectMapper mapper = new ObjectMapper();
        byte[] serializedFunction = null;
        try {
            serializedFunction = mapper.writeValueAsBytes(functionDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return serializedFunction;
    }

    /**
     * Maps a function to a function dto object
     *
     * @param function the function to map
     * @return the {@link FunctionDto} object
     */
    public static FunctionDto mapFunctionToDto(Function function) {
        FunctionDto functionDto = new FunctionDto();
        functionDto.setName(function.getName());
        functionDto.setExecutionId(function.getExecutionId());
        functionDto.setType(functionDto.getType());
        functionDto.setInputData(function.getInputData());
        functionDto.setOutputData(function.getOutputData());
        functionDto.setRunStatus(function.getRunStatus());
        functionDto.setServiceUrl(function.getServiceUrl());
        functionDto.setStepInstanceId(function.getStepInstanceId());
        functionDto.setWorkflowInstanceId(function.getWorkflowInstanceId());
        return functionDto;
    }


    public static void pause(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
