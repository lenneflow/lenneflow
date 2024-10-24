package de.lenneflow.functionservice.controller;


import de.lenneflow.functionservice.dto.FunctionDTO;
import de.lenneflow.functionservice.exception.ResourceNotFoundException;
import de.lenneflow.functionservice.feignclients.WorkerServiceClient;
import de.lenneflow.functionservice.feignmodels.KubernetesCluster;
import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.repository.FunctionRepository;
import de.lenneflow.functionservice.util.Validator;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/functions")
public class FunctionController {

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
        Function function = createFunction(functionDTO);
        function.setUid(UUID.randomUUID().toString());
        validator.validateFunction(function);
        function.setCreationTime(LocalDateTime.now());
        function.setUpdateTime(LocalDateTime.now());
        Function savedFunction = functionRepository.save(function);
        if(!function.isLazyDeployment()){
            new Thread(() -> deploymentController.deployFunctionImageToWorker(savedFunction)).start();
        }
        return savedFunction;
    }

    @PostMapping("/{id}")
    public void updateFunction(@RequestBody Function function, @PathVariable String id) {
        if(function == null) {
            throw new ResourceNotFoundException("Function not found");
        }
        function.setUpdateTime(LocalDateTime.now());
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
           throw new ResourceNotFoundException("Function not found");
        }
        functionRepository.delete(function);
    }

    private Function createFunction(FunctionDTO functionDTO) {
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
        function.setCreationTime(LocalDateTime.now());
        function.setUpdateTime(LocalDateTime.now());
        return function;
    }
}
