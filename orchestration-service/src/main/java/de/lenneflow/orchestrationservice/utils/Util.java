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

    private static final ObjectMapper mapper = new ObjectMapper();

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


    /**
     * Validates a json object against a given json schema.
     *
     * @param jsonSchema the json schema
     * @param version    the schema version
     * @param payload    the json object to validate
     * @return te result of the validation
     */
    public static boolean validateJson(String jsonSchema, JsonSchemaVersion version, String payload) {
        try {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.valueOf(version.toString()));
            JsonSchema schema = factory.getSchema(jsonSchema);
            JsonNode jsonNode = mapper.readTree(payload);
            Set<ValidationMessage> errors = schema.validate(jsonNode);
            return errors.isEmpty();
        } catch (JsonProcessingException e) {
            throw new InternalServiceException("Parse error " + e.getMessage());
        }
    }
}
