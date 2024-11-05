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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/function")
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

    @Operation(summary = "Get a function by id", description = "Returns a function as per the id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    })
    @GetMapping("/{uid}")
    public Function getFunctionById(@PathVariable("uid") @Parameter(name = "uid", description = "Function uid") String uid) {
        return functionRepository.findByUid(uid);
    }

    @Operation(summary = "Get a function by name", description = "Returns a function as per the name")
    @GetMapping("/name/{function-name}")
    public Function getFunctionByName(@PathVariable("function-name")  @Parameter(name = "function-name", description = "Function name") String name) {
        return functionRepository.findByName(name);
    }

    @GetMapping("/list")
    public List<Function> getAllFunctions() {
        return functionRepository.findAll();
    }

    @Operation(summary = "Create a new Function")
    @PostMapping("/create")
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

    @Operation(summary = "Undeploy a function", description = "")
    @GetMapping("/{uid}/undeploy")
    public void unDeployFunction(@PathVariable("uid") String functionId) {
        Function function = functionRepository.findByUid(functionId);
        deploymentController.undeployFunction(function);
        function.setDeploymentState(DeploymentState.UNDEPLOYED);
        functionRepository.save(function);
    }

    @Operation(summary = "Checks the connection to a cluster", description = "")
    @GetMapping(value = "/cluster/{uid}/check-connection")
    @ResponseStatus(value = HttpStatus.OK)
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

    @Operation(summary = "Creates a new json schema", description = "")
    @PostMapping("/json-schema/create")
    public JsonSchema addJsonSchema(@RequestBody JsonSchemaDTO jsonSchemaDto) {
        JsonSchema jsonSchema = ObjectMapper.mapToJsonSchema(jsonSchemaDto);
        jsonSchema.setUid(UUID.randomUUID().toString());
        jsonSchema.setCreated(LocalDateTime.now());
        jsonSchema.setUpdated(LocalDateTime.now());
        validator.validateJsonSchema(jsonSchema);
        return jsonSchemaRepository.save(jsonSchema);
    }

    @Operation(summary = "Get all json schema", description = "")
    @GetMapping("/json-schema/list")
    public List<JsonSchema> getJsonSchemaList() {
        return jsonSchemaRepository.findAll();
    }

    @Operation(summary = "Get a json schema by UID", description = "")
    @GetMapping("/json-schema/{uid}")
    public JsonSchema getJsonSchema(@PathVariable String uid) {
        return jsonSchemaRepository.findByUid(uid);
    }


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

}
