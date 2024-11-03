package de.lenneflow.functionservice.controller;


import de.lenneflow.functionservice.dto.FunctionDTO;
import de.lenneflow.functionservice.enums.DeploymentState;
import de.lenneflow.functionservice.exception.ResourceNotFoundException;
import de.lenneflow.functionservice.feignclients.WorkerServiceClient;
import de.lenneflow.functionservice.feignmodels.KubernetesCluster;
import de.lenneflow.functionservice.helpercomponents.DeploymentController;
import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.model.JsonSchema;
import de.lenneflow.functionservice.repository.FunctionRepository;
import de.lenneflow.functionservice.repository.JsonSchemaRepository;
import de.lenneflow.functionservice.util.ObjectMapper;
import de.lenneflow.functionservice.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Central REST Controller for the function service
 * @author Idrissa Ganemtore
 */

@RestController
@RequestMapping("/api/functions")
public class FunctionController {

    private static final Logger logger = LoggerFactory.getLogger(FunctionController.class);

    final
    FunctionRepository functionRepository;
    final Validator validator;
    final DeploymentController deploymentController;
    final WorkerServiceClient workerServiceClient;
    final JsonSchemaRepository jsonSchemaRepository;

    public FunctionController(FunctionRepository functionRepository, Validator validator, DeploymentController deploymentController, WorkerServiceClient workerServiceClient, JsonSchemaRepository jsonSchemaRepository) {
        this.functionRepository = functionRepository;
        this.validator = validator;
        this.deploymentController = deploymentController;
        this.workerServiceClient = workerServiceClient;
        this.jsonSchemaRepository = jsonSchemaRepository;
    }

    @GetMapping("/{uid}")
    public Function getFunctionById(@PathVariable String uid) {
        return functionRepository.findByUid(uid);
    }

    @GetMapping
    public Function getFunctionByName(@RequestParam(value = "name") String name) {
        return functionRepository.findByName(name);
    }

    @GetMapping("/list")
    public List<Function> getAllFunctions() {
        return functionRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Function addFunction(@RequestBody FunctionDTO functionDTO) {
        validator.validate(functionDTO);
        Function function = ObjectMapper.mapToFunction(functionDTO);
        function.setUid(UUID.randomUUID().toString());
        function.setInputSchema(jsonSchemaRepository.findByUid(functionDTO.getInputSchemaUid()));
        function.setOutputSchema(jsonSchemaRepository.findByUid(functionDTO.getOutputSchemaUid()));
        validator.validate(function);
        function.setCreated(LocalDateTime.now());
        function.setUpdated(LocalDateTime.now());
        Function savedFunction = functionRepository.save(function);
        if(!function.isLazyDeployment()){
            new Thread(() -> deploymentController.deployFunctionImageToWorker(savedFunction)).start();
        }
        return savedFunction;
    }

    @PostMapping("/{id}")
    public void updateFunction(@RequestBody Function function, @PathVariable String id) {
        //TODO
        if(function == null) {
            logger.error("function is null");
            throw new ResourceNotFoundException("Function not found");
        }
        function.setUpdated(LocalDateTime.now());
        functionRepository.save(function);
    }

    @GetMapping( "/deploy-function/function-id/{function-id}")
    public void deployFunction(@PathVariable("function-id") String functionId) {
        Function function = functionRepository.findByUid(functionId);
        deploymentController.deployFunctionImageToWorker(function);
    }

    @GetMapping( "/undeploy-function/function-id/{function-id}")
    public void unDeployFunction(@PathVariable("function-id") String functionId) {
        Function function = functionRepository.findByUid(functionId);
        deploymentController.undeployFunction(function);
        function.setDeploymentState(DeploymentState.UNDEPLOYED);
        functionRepository.save(function);
    }

    @GetMapping(value = "/cluster/{id}/check-connection")
    @ResponseStatus(value = HttpStatus.OK)
    public void checkConnection(@PathVariable String id) {
        KubernetesCluster foundKubernetesCluster = workerServiceClient.getKubernetesClusterById(id);
        if(foundKubernetesCluster == null) {
            throw new ResourceNotFoundException("KUBERNETES_CLUSTER_NOT_FOUND");
        }
        deploymentController.checkConnectionToKubernetes(foundKubernetesCluster);
    }


    @DeleteMapping("/{id}")
    public void deleteFunction(@PathVariable String id) {
        Function function = functionRepository.findByUid(id);
        if (function == null) {
            logger.error("Function is null");
           throw new ResourceNotFoundException("Function not found");
        }
        if(function.getDeploymentState() == DeploymentState.DEPLOYED){
            deploymentController.undeployFunction(function);
        }
        functionRepository.delete(function);
    }

    @PostMapping("/json-schema")
    public JsonSchema addJsonSchema(@RequestBody JsonSchema jsonSchema) {
        jsonSchema.setUid(UUID.randomUUID().toString());
        jsonSchema.setCreated(LocalDateTime.now());
        jsonSchema.setUpdated(LocalDateTime.now());
        validator.validateJsonSchema(jsonSchema);
        return jsonSchemaRepository.save(jsonSchema);
    }

    @GetMapping("/json-schema/list")
    public List<JsonSchema> getJsonSchemaList() {
        return jsonSchemaRepository.findAll();
    }

    @GetMapping("/json-schema/{uid}")
    public JsonSchema getJsonSchema(@PathVariable String uid) {
        return jsonSchemaRepository.findByUid(uid);
    }

}
