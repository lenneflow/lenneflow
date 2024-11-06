package de.lenneflow.functionservice.util;

import com.networknt.schema.*;
import de.lenneflow.functionservice.dto.FunctionDTO;
import de.lenneflow.functionservice.enums.JsonSchemaVersion;
import de.lenneflow.functionservice.exception.InternalServiceException;
import de.lenneflow.functionservice.exception.PayloadNotValidException;
import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.model.JsonSchema;
import de.lenneflow.functionservice.repository.FunctionRepository;
import de.lenneflow.functionservice.repository.JsonSchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Validator class.
 * @author Idrissa Ganemtore
 */
@Component
public class Validator {

    private static final Logger logger = LoggerFactory.getLogger(Validator.class);

    final
    FunctionRepository functionRepository;
    private final JsonSchemaRepository jsonSchemaRepository;

    public Validator(FunctionRepository functionRepository, JsonSchemaRepository jsonSchemaRepository) {
        this.functionRepository = functionRepository;
        this.jsonSchemaRepository = jsonSchemaRepository;
    }

    /**
     * Validates the function object
     * @param function the function to validate
     */
    public void validate(Function function) {
       if(function.getUid() == null || function.getUid().isEmpty()) {
           throw new InternalServiceException("Function uid is mandatory");
       }
       if(function.getInputSchema() == null) {
           throw new InternalServiceException("Input schema is mandatory");
       }
       if(function.getOutputSchema() == null) {
           throw new InternalServiceException("Output schema is mandatory");
       }
    }


    /**
     * Validates the functionDTO object
     * @param functionDTO the functionDTO to validate
     */
    public void validate(FunctionDTO functionDTO) {
        checkMandatoryFields(functionDTO);
        checkResourceRequests(functionDTO);
        checkSchema(functionDTO);
        checkUniqueValues(functionDTO);
    }

    private void checkResourceRequests(FunctionDTO functionDTO) {
        String cpuRequest = functionDTO.getCpuRequest();
        String memoryRequest = functionDTO.getMemoryRequest();
        if(cpuRequest != null && !cpuRequest.isEmpty()) {
            if(cpuRequest.toLowerCase().endsWith("m")){
                cpuRequest = cpuRequest.toLowerCase().replace("m", "").trim();
            }
            try {
                Double.parseDouble(cpuRequest);
            }catch (NumberFormatException e){
                throw new PayloadNotValidException("The CPU request is not valid! It must be in the form (0.5), (1) or (250m) ");
            }
        }
        if(memoryRequest != null && !memoryRequest.isEmpty()) {
            if(!memoryRequest.toLowerCase().endsWith("mi")){
                throw new PayloadNotValidException("The Memory request is not valid! It must be in the form (250Mi) ");
            }
            memoryRequest = memoryRequest.toLowerCase().replace("mi", "").trim();
            try {
                Integer.parseInt(memoryRequest);
            }catch (NumberFormatException e){
                throw new PayloadNotValidException("The Memory request is not valid! It must be in the form (250Mi) ");
            }
        }
    }


    private void checkSchema(FunctionDTO functionDTO)  {
        if(functionDTO.getInputSchemaUid() == null || functionDTO.getInputSchemaUid().isEmpty()){
            throw new PayloadNotValidException("Please provide the input data schema uid!");
        }
        JsonSchema inputSchema = jsonSchemaRepository.findByUid(functionDTO.getInputSchemaUid());
        if(inputSchema == null){
            throw new PayloadNotValidException("input data schema does not exist!");
        }
        if(functionDTO.getOutputSchemaUid() == null || functionDTO.getOutputSchemaUid().isEmpty()){
            throw new PayloadNotValidException("Please provide the output data schema uid!");
        }
        JsonSchema outputSchema = jsonSchemaRepository.findByUid(functionDTO.getOutputSchemaUid());
        if(outputSchema == null){
            throw new PayloadNotValidException("output data schema does not exist!");
        }
    }

    /**
     * Check all unique values of the function.
     * @param function function
     */
    private void checkUniqueValues(FunctionDTO function) {
        if(functionRepository.findByName(function.getName()) != null){
            String logMessage = String.format("Function with name '%s' already exists", function.getName());
            logger.error(logMessage);
            throw new PayloadNotValidException(logMessage);
        }
    }

    /**
     * Check if all mandatory fields are present.
     * @param function function to validate
     */
    private void checkMandatoryFields(FunctionDTO function) {
        if (function.getName() == null || function.getName().isEmpty()) {
            logger.error("function name is mandatory");
            throw new PayloadNotValidException("Function Name is required");
        }
        if(!function.getName().toLowerCase().equals(function.getName())){
            throw new PayloadNotValidException("Function name is incorrect! It must be lowercase");
        }
        if(function.getImageName() == null || function.getImageName().isEmpty()) {
            logger.error("function image name is mandatory");
            throw new PayloadNotValidException("Image Name is required");
        }
        if(function.getPackageRepository() == null) {
            logger.error("function package repository is mandatory");
            throw new PayloadNotValidException("Package Repository is required");
        }
        if(function.getType() == null || function.getType().isEmpty()) {
            logger.error("function type is mandatory");
            throw new PayloadNotValidException("Function Type is required");
        }
    }


    /**
     * validates the input schema specified in the function.
     * @param jsonSchema the schema to validate
     */
    public void validateJsonSchema(JsonSchema jsonSchema) {
        try {
            JsonSchemaVersion version = jsonSchema.getSchemaVersion();
            SpecVersion.VersionFlag versionFlag = SpecVersion.VersionFlag.valueOf(version.toString());
            JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(versionFlag);
            SchemaValidatorsConfig.Builder builder = SchemaValidatorsConfig.builder();
            SchemaValidatorsConfig config = builder.build();
            com.networknt.schema.JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(getSchemaId(versionFlag)), config);
            Set<ValidationMessage> assertions = schema.validate(jsonSchema.getSchema(), InputFormat.JSON, executionContext -> executionContext.getExecutionConfig().setFormatAssertionsEnabled(true));
            if (!assertions.isEmpty()) {
                StringBuilder message = new StringBuilder();
                assertions.forEach(x -> message.append(x.getMessage()).append("\n"));
                throw new PayloadNotValidException("The json schema " + jsonSchema.getName() + " is not valid!\n" + message);
            }
        } catch (Exception e) {
            throw new PayloadNotValidException("Schema parse error " + e.getMessage());
        }

    }

    private String getSchemaId(SpecVersion.VersionFlag versionFlag) {
        switch (versionFlag) {
            case V4 -> {
                return SchemaId.V4;
            }
            case V6 -> {
                return SchemaId.V6;
            }
            case V7 -> {
                return SchemaId.V7;
            }
            case V201909 -> {
                return SchemaId.V201909;
            }
            case V202012 -> {
                return SchemaId.V202012;
            }
        }
        throw new InternalServiceException("Schema version " + versionFlag + " is not known!");
    }
}
