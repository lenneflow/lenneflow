package de.lenneflow.orchestrationservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lenneflow.orchestrationservice.dto.FunctionDto;
import de.lenneflow.orchestrationservice.feignmodels.Function;

import java.io.IOException;


public class Util {

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
}
