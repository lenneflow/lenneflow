package de.lenneflow.functionservice.util;

import de.lenneflow.functionservice.dto.FunctionDTO;
import de.lenneflow.functionservice.model.Function;

/**
 * Object mapper helper class. The main purpose is to map data transfer objects to the entity objects.
 * @author Idrissa Ganemtore
 */
public class ObjectMapper {


   private ObjectMapper(){
    }

    /**
     * Maps a function dto to a function
     * @param functionDTO the dto to map
     * @return the function object
     */
    public static Function mapToFunction(FunctionDTO functionDTO) {
        Function function = new Function();
        function.setName(functionDTO.getName());
        function.setDescription(functionDTO.getDescription());
        function.setType(functionDTO.getType());
        function.setPackageRepository(functionDTO.getPackageRepository());
        function.setResourcePath(functionDTO.getResourcePath());
        function.setServicePort(functionDTO.getServicePort());
        function.setLazyDeployment(functionDTO.isLazyDeployment());
        function.setImageName(functionDTO.getImageName());
        function.setInputSchema(functionDTO.getInputSchema());
        function.setOutputSchema(functionDTO.getOutputSchema());
        return function;
    }
}
