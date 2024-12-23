package de.lenneflow.functionservice.util;

import de.lenneflow.functionservice.dto.FunctionDTO;
import de.lenneflow.functionservice.dto.JsonSchemaDTO;
import de.lenneflow.functionservice.enums.JsonSchemaVersion;
import de.lenneflow.functionservice.enums.PackageRepository;
import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.model.JsonSchema;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectMapperTest {

    @Test
    void mapToFunction_shouldMapAllFieldsCorrectly() {
        FunctionDTO functionDTO = new FunctionDTO();
        functionDTO.setName("testFunction");
        functionDTO.setDescription("testDescription");
        functionDTO.setType("testType");
        functionDTO.setCpuRequest("500m");
        functionDTO.setMemoryRequest("256Mi");
        functionDTO.setPackageRepository(PackageRepository.DOCKER_HUB);
        functionDTO.setResourcePath("/testPath");
        functionDTO.setServicePort(8080);
        functionDTO.setLazyDeployment(true);
        functionDTO.setImageName("testImage");

        Function function = ObjectMapper.mapToFunction(functionDTO);

        assertEquals("testFunction", function.getName());
        assertEquals("testDescription", function.getDescription());
        assertEquals("testType", function.getType());
        assertEquals("500m", function.getCpuRequest());
        assertEquals("256Mi", function.getMemoryRequest());
        assertEquals(PackageRepository.DOCKER_HUB, function.getPackageRepository());
        assertEquals("/testPath", function.getResourcePath());
        assertEquals(8080, function.getServicePort());
        assertTrue(function.isLazyDeployment());
        assertEquals("testImage", function.getImageName());
    }

    @Test
    void mapToFunction_shouldHandleNullFields() {
        FunctionDTO functionDTO = new FunctionDTO();

        Function function = ObjectMapper.mapToFunction(functionDTO);

        assertNull(function.getName());
        assertNull(function.getDescription());
        assertNull(function.getType());
        assertNull(function.getCpuRequest());
        assertNull(function.getMemoryRequest());
        assertNull(function.getPackageRepository());
        assertNull(function.getResourcePath());
        assertEquals(0, function.getServicePort());
        assertFalse(function.isLazyDeployment());
        assertNull(function.getImageName());
    }

    @Test
    void mapToJsonSchema_shouldMapAllFieldsCorrectly() {
        JsonSchemaDTO jsonSchemaDTO = new JsonSchemaDTO();
        jsonSchemaDTO.setSchema("testSchema");
        jsonSchemaDTO.setDescription("testDescription");
        jsonSchemaDTO.setName("testName");
        jsonSchemaDTO.setSchemaVersion(JsonSchemaVersion.V4);

        JsonSchema jsonSchema = ObjectMapper.mapToJsonSchema(jsonSchemaDTO);

        assertEquals("testSchema", jsonSchema.getSchema());
        assertEquals("testDescription", jsonSchema.getDescription());
        assertEquals("testName", jsonSchema.getName());
        assertEquals(JsonSchemaVersion.V4, jsonSchema.getSchemaVersion());
    }

    @Test
    void mapToJsonSchema_shouldHandleNullFields() {
        JsonSchemaDTO jsonSchemaDTO = new JsonSchemaDTO();

        JsonSchema jsonSchema = ObjectMapper.mapToJsonSchema(jsonSchemaDTO);

        assertNull(jsonSchema.getSchema());
        assertNull(jsonSchema.getDescription());
        assertNull(jsonSchema.getName());
        assertNull(jsonSchema.getSchemaVersion());
    }
}