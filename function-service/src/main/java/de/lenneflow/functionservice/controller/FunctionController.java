package de.lenneflow.functionservice.controller;


import de.lenneflow.functionservice.dto.FunctionDTO;
import de.lenneflow.functionservice.dto.JsonSchemaDTO;
import de.lenneflow.functionservice.enums.DeploymentState;
import de.lenneflow.functionservice.exception.InternalServiceException;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Central REST Controller for the function service
 *
 * @author Idrissa Ganemtore
 */

@RestController
@RequestMapping("/api/functions")
@Tag(name = "Functions API")
public class FunctionController {

    private static final Logger logger = LoggerFactory.getLogger(FunctionController.class);

    final FunctionRepository functionRepository;
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

    @Operation(summary = "Get a function by id")
    @GetMapping("/{uid}")
    public Function getFunctionById(@PathVariable("uid") @Parameter(name = "uid", description = "Function uid") String uid) {
        return functionRepository.findByUid(uid);
    }

    @Operation(summary = "Get a function by name", description = "Returns a function as per the name")
    @GetMapping("/name/{function-name}")
    public Function getFunctionByName(@PathVariable("function-name") @Parameter(name = "function-name", description = "Function name") String name) {
        return functionRepository.findByName(name);
    }

    @Operation(summary = "Get all functions")
    @GetMapping("/list")
    public List<Function> getAllFunctions() {

        return functionRepository.findAll();
    }

    @Operation(summary = "Create a new Function")
    @PostMapping("/create")
    public Function addFunction(@RequestBody FunctionDTO functionDTO) {
        validator.validate(functionDTO);
        Function function = ObjectMapper.mapToFunction(functionDTO);
        checkAndAllocateResourcesRequest(function);
        function.setUid(UUID.randomUUID().toString());
        function.setInputSchema(jsonSchemaRepository.findByUid(functionDTO.getInputSchemaUid()));
        function.setOutputSchema(jsonSchemaRepository.findByUid(functionDTO.getOutputSchemaUid()));
        validator.validate(function);
        function.setCreated(LocalDateTime.now());
        function.setUpdated(LocalDateTime.now());
        Function savedFunction = functionRepository.save(function);
        if (!function.isLazyDeployment()) {
            new Thread(() -> deploymentController.deployFunctionImageToWorker(savedFunction)).start();
        }
        return savedFunction;
    }

    @Operation(summary = "Updates an existing function")
    @PostMapping("/{uid}/update")
    public void updateFunction(@RequestBody FunctionDTO functionDTO, @PathVariable String uid) {
        validator.validate(functionDTO);
        Function foundFunction = functionRepository.findByUid(uid);
        if (foundFunction == null) {
            logger.error("function with UID {} not found", uid);
            throw new ResourceNotFoundException("function with UID " + uid + " not found");
        }
        Function function = ObjectMapper.mapToFunction(functionDTO);
        checkAndAllocateResourcesRequest(function);
        function.setUid(foundFunction.getUid());
        if (foundFunction.getDeploymentState() == DeploymentState.UNDEPLOYED) {
            function.setUpdated(LocalDateTime.now());
            function.setDeploymentState(DeploymentState.UNDEPLOYED);
            functionRepository.save(function);
            return;
        }
        if (foundFunction.getDeploymentState() == DeploymentState.DEPLOYING) {
            throw new InternalServiceException("Function is currently deploying! No change is allowed!");
        }
        if (isNewDeploymentNecessary(foundFunction, function)) {
            deploymentController.undeployFunction(foundFunction);
            function.setUpdated(LocalDateTime.now());
            Function savedFunction = functionRepository.save(function);
            new Thread(() -> deploymentController.deployFunctionImageToWorker(savedFunction)).start();
            return;
        }
        function.setUpdated(LocalDateTime.now());
        functionRepository.save(function);

    }

    @Operation(summary = "Deploy a function", description = "Deploys a function that has already been created.")
    @GetMapping("/{uid}/deploy")
    public void deployFunction(@PathVariable("uid") String functionId) {
        Function function = functionRepository.findByUid(functionId);
        deploymentController.deployFunctionImageToWorker(function);
    }

    @Operation(summary = "Undeploy a function")
    @GetMapping("/{uid}/undeploy")
    public void unDeployFunction(@PathVariable("uid") String functionId) {
        Function function = functionRepository.findByUid(functionId);
        deploymentController.undeployFunction(function);
        function.setDeploymentState(DeploymentState.UNDEPLOYED);
        functionRepository.save(function);
    }

    @Operation(summary = "Checks the connection to a cluster")
    @GetMapping(value = "/cluster/{uid}/check-connection")
    public void checkConnection(@PathVariable String uid) {
        KubernetesCluster foundKubernetesCluster = workerServiceClient.getKubernetesClusterById(uid);
        if (foundKubernetesCluster == null) {
            throw new ResourceNotFoundException("KUBERNETES_CLUSTER_NOT_FOUND");
        }
        deploymentController.checkConnectionToKubernetes(foundKubernetesCluster);
    }


    @Operation(summary = "Deletes a function by id", description = "Undeploy and delete the specified function")
    @DeleteMapping("/{uid}")
    public void deleteFunction(@PathVariable String uid) {
        Function function = functionRepository.findByUid(uid);
        if (function == null) {
            logger.error("Function is null");
            throw new ResourceNotFoundException("Function not found");
        }
        if (function.getDeploymentState() == DeploymentState.DEPLOYED) {
            deploymentController.undeployFunction(function);
        }
        functionRepository.delete(function);
    }

    @Operation(summary = "Creates a new json schema")
    @PostMapping("/json-schema/create")
    public JsonSchema addJsonSchema(@RequestBody JsonSchemaDTO jsonSchemaDto) {
        JsonSchema jsonSchema = ObjectMapper.mapToJsonSchema(jsonSchemaDto);
        jsonSchema.setUid(UUID.randomUUID().toString());
        jsonSchema.setCreated(LocalDateTime.now());
        jsonSchema.setUpdated(LocalDateTime.now());
        validator.validateJsonSchema(jsonSchema);
        return jsonSchemaRepository.save(jsonSchema);
    }

    @Operation(summary = "Get all json schema")
    @GetMapping("/json-schema/list")
    public List<JsonSchema> getJsonSchemaList() {

        return jsonSchemaRepository.findAll();
    }

    @Operation(summary = "Get a json schema by UID")
    @GetMapping("/json-schema/{uid}")
    public JsonSchema getJsonSchema(@PathVariable String uid) {
        return jsonSchemaRepository.findByUid(uid);
    }

    @Operation(summary = "Get a json schema by UID")
    @DeleteMapping("/json-schema/{uid}")
    public void deleteJsonSchema(@PathVariable String uid) {
        JsonSchema found = jsonSchemaRepository.findByUid(uid);
        jsonSchemaRepository.delete(found);
    }


    /**
     * The update of some fields in the function require a new deployment of the function
     * Here is the check if a new deployment is necessary
     * @param oldFunction the current function
     * @param newFunction the new function
     * @return true if necessary, false otherwise
     */
    private boolean isNewDeploymentNecessary(Function oldFunction, Function newFunction) {
        if (!oldFunction.getImageName().equals(newFunction.getImageName())) {
            return true;
        }
        if (oldFunction.getServicePort() != newFunction.getServicePort()) {
            return true;
        }
        if (!oldFunction.getName().equals(newFunction.getName())) {
            return true;
        }
        if (!oldFunction.getResourcePath().equals(newFunction.getResourcePath())) {
            return true;
        }
        return oldFunction.getAssignedHostPort() != newFunction.getAssignedHostPort();
    }

    /**
     * If the resources requests are not provided by the API User, default ones are provided here.
     * @param function the function object
     */
    private void checkAndAllocateResourcesRequest(Function function) {
        if (function.getCpuRequest() == null || function.getCpuRequest().isEmpty()) {
            function.setCpuRequest("250m");
        }
        if (function.getMemoryRequest() == null || function.getMemoryRequest().isEmpty()) {
            function.setMemoryRequest("128Mi");
        }
    }

}
