package de.lenneflow.orchestrationservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import de.lenneflow.orchestrationservice.enums.JsonSchemaVersion;
import de.lenneflow.orchestrationservice.exception.PayloadNotValidException;
import de.lenneflow.orchestrationservice.model.GlobalInputData;

import java.util.Map;
import java.util.Set;

public class Validator {

    private Validator(){}

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Validates a json object against a given json schema.
     *
     * @param jsonSchema the json schema
     * @param version    the schema version
     * @param dataToValidate    the object to validate
     * @return te result of the validation
     */
    public static void validateJsonData(String jsonSchema, JsonSchemaVersion version, Map<String, Object> dataToValidate) {
        try {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.valueOf(version.toString()));
            JsonSchema schema = factory.getSchema(jsonSchema);
            JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(dataToValidate));
            Set<ValidationMessage> errors = schema.validate(jsonNode);
            if(!errors.isEmpty()){
                StringBuilder message = new StringBuilder();
                errors.forEach(x-> message.append(x.getMessage()).append("\n"));
                throw new PayloadNotValidException("The json input is not valid" + message);
            }
        } catch (JsonProcessingException e) {
            throw new PayloadNotValidException("Input data parse error " + e.getMessage());
        }
    }


    public static void validate(GlobalInputData globalInputData){
        if(globalInputData.getInputData() == null){
            throw new PayloadNotValidException("Input data is null");
        }
        if(globalInputData.getName() == null || globalInputData.getName().isEmpty()){
            throw new PayloadNotValidException("Name is null or empty");
        }
    }
}
