package de.lenneflow.functionservice.controller;


import de.lenneflow.functionservice.dto.FunctionDTO;
import de.lenneflow.functionservice.exception.ResourceNotFoundException;
import de.lenneflow.functionservice.feignclients.WorkerServiceClient;
import de.lenneflow.functionservice.feignmodels.KubernetesCluster;
import de.lenneflow.functionservice.helpercomponents.DeploymentController;
import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.repository.FunctionRepository;
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

    public FunctionController(FunctionRepository functionRepository, Validator validator, DeploymentController deploymentController, WorkerServiceClient workerServiceClient) {
        this.functionRepository = functionRepository;
        this.validator = validator;
        this.deploymentController = deploymentController;
        this.workerServiceClient = workerServiceClient;
    }

    @GetMapping("/{uid}")
    public Function getFunctionById(@PathVariable String uid) {
        return functionRepository.findByUid(uid);
    }

    @GetMapping
    public Function getFunctionByName(@RequestParam(value = "name") String name) {
        return functionRepository.findByName(name);
    }

    @GetMapping("/all")
    public List<Function> getAllFunctions() {
        return functionRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Function addFunction(@RequestBody FunctionDTO functionDTO) {
        Function function = ObjectMapper.mapToFunction(functionDTO);
        function.setUid(UUID.randomUUID().toString());
        validator.validateFunction(function);
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
        functionRepository.delete(function);
    }

}
