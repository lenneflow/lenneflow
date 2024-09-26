package de.lenneflow.functionservice.controller;


import de.lenneflow.functionservice.dto.FunctionDTO;
import de.lenneflow.functionservice.exception.ResourceNotFoundException;
import de.lenneflow.functionservice.feignclients.WorkerServiceClient;
import de.lenneflow.functionservice.feignmodels.KubernetesCluster;
import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.repository.FunctionRepository;
import de.lenneflow.functionservice.util.Validator;
import io.swagger.v3.oas.annotations.Hidden;
import org.modelmapper.ModelMapper;
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
    final ModelMapper modelMapper;
    final KubernetesController kubernetesController;
    final WorkerServiceClient workerServiceClient;

    public FunctionController(FunctionRepository functionRepository, Validator validator, KubernetesController kubernetesController, WorkerServiceClient workerServiceClient) {
        this.functionRepository = functionRepository;
        this.validator = validator;
        this.kubernetesController = kubernetesController;
        this.workerServiceClient = workerServiceClient;
        modelMapper = new ModelMapper();
    }

    @Hidden
    @GetMapping(value={ "/check"})
    public String checkService() {
        return "Welcome to the Function Service! Everything is working fine!";
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
        Function function = modelMapper.map(functionDTO, Function.class);
        function.setUid(UUID.randomUUID().toString());
        validator.validateFunction(function);
        function.setCreationTime(LocalDateTime.now());
        function.setUpdateTime(LocalDateTime.now());
        Function savedFunction = functionRepository.save(function);
        if(!function.isLazyDeployment()){
            new Thread(() -> kubernetesController.deployFunctionImageToWorker(savedFunction)).start();
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

    @GetMapping(value = "/deploy-function", params = "function-id")
    @ResponseStatus(value = HttpStatus.OK)
    public void deployFunction(@RequestParam(name = "function-id") String functionId) {
        Function function = functionRepository.findByUid(functionId);
        kubernetesController.deployFunctionImageToWorker(function);
    }

    @GetMapping(value = "/cluster/{id}/check-connection")
    @ResponseStatus(value = HttpStatus.OK)
    public void checkConnection(@PathVariable String id) {
        KubernetesCluster foundKubernetesCluster = workerServiceClient.getKubernetesClusterById(id);
        if(foundKubernetesCluster == null) {
            throw new ResourceNotFoundException("KUBERNETES_CLUSTER_NOT_FOUND");
        }
        kubernetesController.checkWorkerConnection(foundKubernetesCluster);
    }


    @DeleteMapping("/{id}")
    public void deleteFunction(@PathVariable String id) {
        Function function = functionRepository.findByUid(id);
        if (function == null) {
           throw new ResourceNotFoundException("Function not found");
        }
        functionRepository.delete(function);
    }
}
