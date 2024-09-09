package de.lenneflow.workerservice.controller;

import com.amazonaws.services.eks.model.Addon;
import de.lenneflow.workerservice.dto.WorkerDTO;
import de.lenneflow.workerservice.enums.WorkerStatus;
import de.lenneflow.workerservice.exception.ResourceNotFoundException;
import de.lenneflow.workerservice.feignclients.FunctionServiceClient;
import de.lenneflow.workerservice.feignmodel.Function;
import de.lenneflow.workerservice.kubernetes.aws.CloudController;
import de.lenneflow.workerservice.model.CloudCluster;
import de.lenneflow.workerservice.model.CloudCredential;
import de.lenneflow.workerservice.model.CloudNodeGroup;
import de.lenneflow.workerservice.model.LocalCluster;
import de.lenneflow.workerservice.repository.CloudClusterRepository;
import de.lenneflow.workerservice.repository.CloudCredentialRepository;
import de.lenneflow.workerservice.repository.CloudNodeGroupRepository;
import de.lenneflow.workerservice.repository.WorkerRepository;
import de.lenneflow.workerservice.kubernetes.KubernetesController;
import de.lenneflow.workerservice.util.PayloadValidator;
import io.swagger.v3.oas.annotations.Hidden;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    final
    WorkerRepository workerRepository;
    final ModelMapper modelMapper;
    final PayloadValidator payloadValidator;
    final FunctionServiceClient functionServiceClient;
    final KubernetesController kubernetesController;
    final CloudClusterRepository cloudClusterRepository;
    final CloudController cloudController;
    final CloudCredentialRepository localCredentialRepository;
    final CloudCredentialRepository cloudCredentialRepository;
    final CloudNodeGroupRepository cloudNodeGroupRepository;

    public WorkerController(WorkerRepository workerRepository, PayloadValidator payloadValidator, FunctionServiceClient functionServiceClient, KubernetesController kubernetesController, CloudClusterRepository cloudClusterRepository, CloudController cloudController, CloudCredentialRepository localCredentialRepository, CloudCredentialRepository cloudCredentialRepository, CloudNodeGroupRepository cloudNodeGroupRepository) {
        this.workerRepository = workerRepository;
        this.payloadValidator = payloadValidator;
        this.functionServiceClient = functionServiceClient;
        this.kubernetesController = kubernetesController;
        this.cloudClusterRepository = cloudClusterRepository;
        this.cloudController = cloudController;
        this.localCredentialRepository = localCredentialRepository;
        this.cloudNodeGroupRepository = cloudNodeGroupRepository;
        modelMapper = new ModelMapper();
        this.cloudCredentialRepository = cloudCredentialRepository;
    }

    @Hidden
    @GetMapping(value={ "/check"})
    public String checkService() {
        return "Welcome to the Worker Service!";
    }

    @PostMapping
    public ResponseEntity<LocalCluster> createNewWorker(@RequestBody WorkerDTO workerDTO) {
        LocalCluster localCluster = modelMapper.map(workerDTO, LocalCluster.class);
        localCluster.setUid(UUID.randomUUID().toString());
        localCluster.setCreated(LocalDateTime.now());
        localCluster.setUpdated(LocalDateTime.now());
        localCluster.setStatus(WorkerStatus.OFFLINE);
        localCluster.setIngressServiceName(localCluster.getName().toLowerCase() + "-ingress");
        payloadValidator.validate(localCluster);
        LocalCluster savedLocalCluster = workerRepository.save(localCluster);
        return new ResponseEntity<>(savedLocalCluster, HttpStatus.CREATED);
    }

    @PostMapping("/clusters/credentials")
    public ResponseEntity<CloudCredential> createCloudClusterCredential(@RequestBody CloudCredential cloudCredential) {
        cloudCredential.setUid(UUID.randomUUID().toString());
        CloudCredential savedCredential = cloudCredentialRepository.save(cloudCredential);
        return new ResponseEntity<>(savedCredential, HttpStatus.OK);
    }

    @PostMapping("/clusters/node-groups")
    public ResponseEntity<CloudNodeGroup> createCloudClusterNodeGroup(@RequestBody CloudNodeGroup cloudNodeGroup) {
        cloudNodeGroup.setUid(UUID.randomUUID().toString());
        CloudNodeGroup savedNodeGroup = cloudNodeGroupRepository.save(cloudNodeGroup);
        if(savedNodeGroup.isCreate()){
            cloudController.createNodeGroup(cloudNodeGroup);
        }
        return new ResponseEntity<>(savedNodeGroup, HttpStatus.OK);
    }

    @GetMapping("/clusters/{uid}/addons/{name}")
    public ResponseEntity<Addon> createCloudClusterAddOn(@PathVariable("uid") String uid, @PathVariable("name") String addonName) {
        CloudCluster cluster = cloudClusterRepository.findByUid(uid);
        Addon addon = cloudController.createClusterAddOn(cluster, addonName);
        return new ResponseEntity<>(addon, HttpStatus.OK);
    }

    @PostMapping("/clusters")
    public ResponseEntity<CloudCluster> createCloudCluster(@RequestBody CloudCluster cloudCluster) {
        cloudCluster.setUid(UUID.randomUUID().toString());
        //TODO set dates and validate (check existence)
        CloudCluster savedCluster  = cloudClusterRepository.save(cloudCluster);
        if(savedCluster.isCreate()){
            cloudController.createCluster(cloudCluster);
        }
        return new ResponseEntity<>(savedCluster, HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<LocalCluster> updateWorker(@RequestBody WorkerDTO workerDTO, @PathVariable String id) {
        LocalCluster localCluster = workerRepository.findByUid(id);
        modelMapper.map(workerDTO, localCluster);
        if(localCluster == null) {
            throw new ResourceNotFoundException("LocalCluster not found");
        }
        payloadValidator.validate(localCluster);
        LocalCluster savedLocalCluster = workerRepository.save(localCluster);
        return new ResponseEntity<>(savedLocalCluster, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocalCluster> getWorker(@PathVariable String id) {
        LocalCluster foundLocalCluster = workerRepository.findByUid(id);
        if(foundLocalCluster == null) {
            throw new ResourceNotFoundException("LocalCluster not found");
        }
        return new ResponseEntity<>(foundLocalCluster, HttpStatus.OK);
    }

    @GetMapping(value = "/deploy-function", params = "function-id")
    @ResponseStatus(value = HttpStatus.OK)
    public void deployFunction(@RequestParam(name = "function-id") String functionId) {
        Function function = functionServiceClient.getFunctionById(functionId);
        kubernetesController.deployFunctionImageToWorker(function);
    }

    @GetMapping(value = "/{id}/check-connection")
    @ResponseStatus(value = HttpStatus.OK)
    public void checkConnection(@PathVariable String id) {
        LocalCluster foundLocalCluster = workerRepository.findByUid(id);
        if(foundLocalCluster == null) {
            throw new ResourceNotFoundException("LocalCluster not found");
        }
        kubernetesController.checkWorkerConnection(foundLocalCluster);
    }

    @GetMapping("/all")
    public List<LocalCluster> getAllWorkers() {
        return workerRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<LocalCluster> deleteWorker(@PathVariable String id) {
        LocalCluster foundLocalCluster = workerRepository.findByUid(id);
        if(foundLocalCluster == null) {
            throw new ResourceNotFoundException("LocalCluster with id " + id + " not found");
        }
        workerRepository.delete(foundLocalCluster);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
