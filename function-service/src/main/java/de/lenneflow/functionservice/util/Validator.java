package de.lenneflow.functionservice.util;

import de.lenneflow.functionservice.exception.PayloadNotValidException;
import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.repository.FunctionRepository;
import org.springframework.stereotype.Component;

@Component
public class Validator {

    final
    FunctionRepository functionRepository;

    public Validator(FunctionRepository functionRepository) {
        this.functionRepository = functionRepository;
    }

    public void validateFunction(Function function) {
        checkMandatoryFields(function);
        validateInputSchema(function);
        checkUniqueValues(function);

    }

    private void checkUniqueValues(Function function) {
        if(functionRepository.findByName(function.getName()) != null){
            throw new PayloadNotValidException("A function with the name " + function.getName()+ " already exists");
        }
    }

    private void validateInputSchema(Function function) {
        String schema = function.getInputSchema();
        //TODO Validate schema

    }


    private void checkMandatoryFields(Function function) {
        if (function.getName() == null || function.getName().isEmpty()) {
            throw new PayloadNotValidException("Function Name is required");
        }
        if(function.getImageName() == null || function.getImageName().isEmpty()) {
            throw new PayloadNotValidException("Image Name is required");
        }
        if(function.getPackageRepository() == null || function.getPackageRepository().isEmpty()) {
            throw new PayloadNotValidException("Package Repository is required");
        }
        if(function.getType() == null || function.getType().isEmpty()) {
            throw new PayloadNotValidException("Function Type is required");
        }
        if(function.getInputSchema() == null || function.getInputSchema().isEmpty()) {
            throw new PayloadNotValidException("Input Schema is required");
        }
    }
}
