package de.lenneflow.orchestrationservice.utils;

import de.lenneflow.orchestrationservice.enums.JsonSchemaVersion;
import de.lenneflow.orchestrationservice.exception.PayloadNotValidException;
import de.lenneflow.orchestrationservice.model.GlobalInputData;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ValidatorTest {

    @Test
    void validateJsonData_validData_noExceptionThrown() {
        String jsonSchema = "{ \"$schema\": \"http://json-schema.org/draft-07/schema#\", \"type\": \"object\", \"properties\": { \"name\": { \"type\": \"string\" } }, \"required\": [ \"name\" ] }";
        JsonSchemaVersion version = JsonSchemaVersion.V7;
        Map<String, Object> dataToValidate = new HashMap<>();
        dataToValidate.put("name", "test");

        assertDoesNotThrow(() -> Validator.validateJsonData(jsonSchema, version, dataToValidate));
    }

    @Test
    void validateJsonData_invalidData_throwsPayloadNotValidException() {
        String jsonSchema = "{ \"$schema\": \"http://json-schema.org/draft-07/schema#\", \"type\": \"object\", \"properties\": { \"name\": { \"type\": \"string\" } }, \"required\": [ \"name\" ] }";
        JsonSchemaVersion version = JsonSchemaVersion.V7;
        Map<String, Object> dataToValidate = new HashMap<>();

        assertThrows(PayloadNotValidException.class, () -> Validator.validateJsonData(jsonSchema, version, dataToValidate));
    }

    @Test
    void validateJsonData_invalidJsonSchema_throwsPayloadNotValidException() {
        String jsonSchema = "{ \"$schema\": \"http://json-schema.org/draft-07/schema#\", \"type\": \"object\", \"properties\": { \"name\": { \"type\": \"string\" } }, \"required\": [ \"name\" ]";
        JsonSchemaVersion version = JsonSchemaVersion.V7;
        Map<String, Object> dataToValidate = new HashMap<>();
        dataToValidate.put("name", "test");

        assertThrows(PayloadNotValidException.class, () -> Validator.validateJsonData(jsonSchema, version, dataToValidate));
    }

    @Test
    void validateJsonData_invalidJsonData_throwsPayloadNotValidException() {
        String jsonSchema = "{ \"$schema\": \"http://json-schema.org/draft-07/schema#\", \"type\": \"object\", \"properties\": { \"name\": { \"type\": \"string\" } }, \"required\": [ \"name\" ] }";
        JsonSchemaVersion version = JsonSchemaVersion.V7;
        Map<String, Object> dataToValidate = new HashMap<>();
        dataToValidate.put("name", new Object());

        assertThrows(PayloadNotValidException.class, () -> Validator.validateJsonData(jsonSchema, version, dataToValidate));
    }

    @Test
    void validate_validGlobalInputData_noExceptionThrown() {
        GlobalInputData globalInputData = new GlobalInputData();
        globalInputData.setInputData(new HashMap<>());
        globalInputData.setName("test");

        assertDoesNotThrow(() -> Validator.validate(globalInputData));
    }

    @Test
    void validate_nullInputData_throwsPayloadNotValidException() {
        GlobalInputData globalInputData = new GlobalInputData();
        globalInputData.setName("test");

        assertThrows(PayloadNotValidException.class, () -> Validator.validate(globalInputData));
    }

    @Test
    void validate_nullName_throwsPayloadNotValidException() {
        GlobalInputData globalInputData = new GlobalInputData();
        globalInputData.setInputData(new HashMap<>());

        assertThrows(PayloadNotValidException.class, () -> Validator.validate(globalInputData));
    }

    @Test
    void validate_emptyName_throwsPayloadNotValidException() {
        GlobalInputData globalInputData = new GlobalInputData();
        globalInputData.setInputData(new HashMap<>());
        globalInputData.setName("");

        assertThrows(PayloadNotValidException.class, () -> Validator.validate(globalInputData));
    }
}