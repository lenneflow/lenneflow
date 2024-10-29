package de.lenneflow.functionservice.util;

import de.lenneflow.functionservice.dto.FunctionDTO;
import de.lenneflow.functionservice.exception.PayloadNotValidException;
import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.model.JsonSchema;
import de.lenneflow.functionservice.repository.FunctionRepository;
import de.lenneflow.functionservice.repository.JsonSchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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


    public void validate(FunctionDTO functionDTO)  {
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
     * Validates the function object
     * @param function the function to validate
     */
    public void validate(Function function) {
        checkMandatoryFields(function);
        //validateJsonSchema(function);
        checkUniqueValues(function);
    }

    /**
     * Check all unique values of the function.
     * @param function function
     */
    private void checkUniqueValues(Function function) {
        if(functionRepository.findByName(function.getName()) != null){
            String logMessage = String.format("Function with name '%s' already exists", function.getName());
            logger.error(logMessage);
            throw new PayloadNotValidException(logMessage);
        }
    }

    /**
     * validates the input schema specified in the function.
     * @param schema the schema to validate
     */
    public void validateJsonSchema(JsonSchema schema) {
        //TODO Validate schema itself

    }


    /**
     * Check if all mandatory fields are present.
     * @param function function to validate
     */
    private void checkMandatoryFields(Function function) {
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
        if(function.getInputSchema() == null) {
            logger.error("function input schema is mandatory");
            throw new PayloadNotValidException("Input Schema is required");
        }
    }
}
