package de.lenneflow.functionservice.util;

import de.lenneflow.functionservice.exception.PayloadNotValidException;
import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.repository.FunctionRepository;
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

    public Validator(FunctionRepository functionRepository) {
        this.functionRepository = functionRepository;
    }

    /**
     * Validates the function object
     * @param function the function to validate
     */
    public void validateFunction(Function function) {
        checkMandatoryFields(function);
        validateInputSchema(function);
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
     * @param function the function to validate
     */
    private void validateInputSchema(Function function) {
        String schema = function.getInputSchema();
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
        if(function.getInputSchema() == null || function.getInputSchema().isEmpty()) {
            logger.error("function input schema is mandatory");
            throw new PayloadNotValidException("Input Schema is required");
        }
    }
}
