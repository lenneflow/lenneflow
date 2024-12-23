package de.lenneflow.functionservice.util;

import de.lenneflow.functionservice.dto.FunctionDTO;
import de.lenneflow.functionservice.enums.JsonSchemaVersion;
import de.lenneflow.functionservice.enums.PackageRepository;
import de.lenneflow.functionservice.exception.InternalServiceException;
import de.lenneflow.functionservice.exception.PayloadNotValidException;
import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.model.JsonSchema;
import de.lenneflow.functionservice.repository.FunctionRepository;
import de.lenneflow.functionservice.repository.JsonSchemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidatorTest {

    @Mock
    private FunctionRepository functionRepository;

    @Mock
    private JsonSchemaRepository jsonSchemaRepository;

    private Validator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new Validator(functionRepository, jsonSchemaRepository);
    }

    @Test
    void validateFunction_shouldThrowExceptionWhenUidIsNull() {
        Function function = new Function();
        function.setUid(null);

        assertThrows(InternalServiceException.class, () -> validator.validate(function));
    }

    @Test
    void validateFunction_shouldThrowExceptionWhenInputSchemaIsNull() {
        Function function = new Function();
        function.setUid("uid");
        function.setInputSchema(null);

        assertThrows(InternalServiceException.class, () -> validator.validate(function));
    }

    @Test
    void validateFunction_shouldThrowExceptionWhenOutputSchemaIsNull() {
        Function function = new Function();
        function.setUid("uid");
        function.setInputSchema(new JsonSchema());
        function.setOutputSchema(null);

        assertThrows(InternalServiceException.class, () -> validator.validate(function));
    }

    @Test
    void validateFunctionDTO_shouldThrowExceptionWhenNameIsNull() {
        FunctionDTO functionDTO = new FunctionDTO();
        functionDTO.setName(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(functionDTO));
    }

    @Test
    void validateFunctionDTO_shouldThrowExceptionWhenNameIsNotLowercase() {
        FunctionDTO functionDTO = new FunctionDTO();
        functionDTO.setName("TestFunction");

        assertThrows(PayloadNotValidException.class, () -> validator.validate(functionDTO));
    }

    @Test
    void validateFunctionDTO_shouldThrowExceptionWhenImageNameIsNull() {
        FunctionDTO functionDTO = new FunctionDTO();
        functionDTO.setName("testfunction");
        functionDTO.setImageName(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(functionDTO));
    }

    @Test
    void validateFunctionDTO_shouldThrowExceptionWhenPackageRepositoryIsNull() {
        FunctionDTO functionDTO = new FunctionDTO();
        functionDTO.setName("testfunction");
        functionDTO.setImageName("image");
        functionDTO.setPackageRepository(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(functionDTO));
    }

    @Test
    void validateFunctionDTO_shouldThrowExceptionWhenTypeIsNull() {
        FunctionDTO functionDTO = new FunctionDTO();
        functionDTO.setName("testfunction");
        functionDTO.setImageName("image");
        functionDTO.setPackageRepository(PackageRepository.DOCKER_HUB);
        functionDTO.setType(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(functionDTO));
    }

    @Test
    void validateFunctionDTO_shouldThrowExceptionWhenCpuRequestIsInvalid() {
        FunctionDTO functionDTO = new FunctionDTO();
        functionDTO.setName("testfunction");
        functionDTO.setImageName("image");
        functionDTO.setPackageRepository(PackageRepository.DOCKER_HUB);
        functionDTO.setType("type");
        functionDTO.setCpuRequest("invalid");

        assertThrows(PayloadNotValidException.class, () -> validator.validate(functionDTO));
    }

    @Test
    void validateFunctionDTO_shouldThrowExceptionWhenMemoryRequestIsInvalid() {
        FunctionDTO functionDTO = new FunctionDTO();
        functionDTO.setName("testfunction");
        functionDTO.setImageName("image");
        functionDTO.setPackageRepository(PackageRepository.DOCKER_HUB);
        functionDTO.setType("type");
        functionDTO.setMemoryRequest("invalid");

        assertThrows(PayloadNotValidException.class, () -> validator.validate(functionDTO));
    }

    @Test
    void validateJsonSchema_shouldThrowExceptionWhenSchemaIsInvalid() {
        JsonSchema jsonSchema = new JsonSchema();
        jsonSchema.setSchemaVersion(JsonSchemaVersion.V4);
        jsonSchema.setSchema("invalid");

        assertThrows(PayloadNotValidException.class, () -> validator.validateJsonSchema(jsonSchema));
    }
}