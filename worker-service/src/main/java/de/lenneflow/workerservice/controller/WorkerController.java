package de.lenneflow.workerservice.controller;

import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.enums.ClusterStatus;
import de.lenneflow.workerservice.exception.ResourceNotFoundException;
import de.lenneflow.workerservice.feignclients.FunctionServiceClient;
import de.lenneflow.workerservice.feignmodel.Function;
import de.lenneflow.workerservice.kubernetes.cloud.CloudController;
import de.lenneflow.workerservice.model.KubernetesCluster;
import de.lenneflow.workerservice.model.CloudCredential;
import de.lenneflow.workerservice.model.CloudNodeGroup;
import de.lenneflow.workerservice.repository.KubernetesClusterRepository;
import de.lenneflow.workerservice.repository.CloudCredentialRepository;
import de.lenneflow.workerservice.repository.CloudNodeGroupRepository;
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

    private static final String KUBERNETES_CLUSTER_NOT_FOUND = "KubernetesCluster not found";

    final ModelMapper modelMapper;
    final PayloadValidator payloadValidator;
    final FunctionServiceClient functionServiceClient;
    final CloudController cloudController;
    final KubernetesController kubernetesController;
    final KubernetesClusterRepository kubernetesClusterRepository;
    final CloudCredentialRepository localCredentialRepository;
    final CloudCredentialRepository cloudCredentialRepository;
    final CloudNodeGroupRepository cloudNodeGroupRepository;

    public WorkerController(PayloadValidator payloadValidator, FunctionServiceClient functionServiceClient, KubernetesClusterRepository kubernetesClusterRepository, CloudController cloudController, KubernetesController kubernetesController1, CloudCredentialRepository localCredentialRepository, CloudCredentialRepository cloudCredentialRepository, CloudNodeGroupRepository cloudNodeGroupRepository) {
        this.payloadValidator = payloadValidator;
        this.functionServiceClient = functionServiceClient;
        this.kubernetesClusterRepository = kubernetesClusterRepository;
        this.cloudController = cloudController;
        this.kubernetesController = kubernetesController1;
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

    @PostMapping("/clusters")
    public ResponseEntity<KubernetesCluster> createKubernetesCluster(@RequestBody KubernetesCluster kubernetesCluster) {
        kubernetesCluster.setUid(UUID.randomUUID().toString());
        kubernetesCluster.setCreated(LocalDateTime.now());
        kubernetesCluster.setUpdated(LocalDateTime.now());
        kubernetesCluster.setStatus(ClusterStatus.OFFLINE);
        kubernetesCluster.setIngressServiceName(kubernetesCluster.getClusterName().toLowerCase() + "-ingress");
        payloadValidator.validate(kubernetesCluster);
        kubernetesClusterRepository.save(kubernetesCluster);
        if(!kubernetesCluster.getCloudProvider().equals(CloudProvider.LOCAL)){
            if(kubernetesCluster.isCreate()){
                kubernetesCluster.setStatus(ClusterStatus.CREATING);
                kubernetesClusterRepository.save(kubernetesCluster);
                cloudController.createCluster(kubernetesCluster);
            }else{
                cloudController.getCluster(kubernetesCluster);
                kubernetesCluster.setStatus(ClusterStatus.CREATED);
                kubernetesClusterRepository.save(kubernetesCluster);
            }
        }
        KubernetesCluster savedKubernetesCluster = kubernetesClusterRepository.save(kubernetesCluster);
        return new ResponseEntity<>(savedKubernetesCluster, HttpStatus.CREATED);
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
    public ResponseEntity<Object> createCloudClusterAddOn(@PathVariable("uid") String uid, @PathVariable("name") String addonName) {
        KubernetesCluster cluster = kubernetesClusterRepository.findByUid(uid);
        Object addon = cloudController.createClusterAddOn(cluster, addonName);
        return new ResponseEntity<>(addon, HttpStatus.OK);
    }


    @PostMapping("/{id}")
    public ResponseEntity<KubernetesCluster> updateWorker(@RequestBody KubernetesCluster kubernetesCluster, @PathVariable String id) {
        if(kubernetesCluster == null) {
            throw new ResourceNotFoundException(KUBERNETES_CLUSTER_NOT_FOUND);
        }
        payloadValidator.validate(kubernetesCluster);
        KubernetesCluster savedKubernetesCluster = kubernetesClusterRepository.save(kubernetesCluster);
        return new ResponseEntity<>(savedKubernetesCluster, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KubernetesCluster> getWorker(@PathVariable String id) {
        KubernetesCluster foundKubernetesCluster = kubernetesClusterRepository.findByUid(id);
        if(foundKubernetesCluster == null) {
            throw new ResourceNotFoundException(KUBERNETES_CLUSTER_NOT_FOUND);
        }
        return new ResponseEntity<>(foundKubernetesCluster, HttpStatus.OK);
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
        KubernetesCluster foundKubernetesCluster = kubernetesClusterRepository.findByUid(id);
        if(foundKubernetesCluster == null) {
            throw new ResourceNotFoundException(KUBERNETES_CLUSTER_NOT_FOUND);
        }
        kubernetesController.checkWorkerConnection(foundKubernetesCluster);
    }

    @GetMapping("/all")
    public List<KubernetesCluster> getAllWorkers() {
        return kubernetesClusterRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<KubernetesCluster> deleteWorker(@PathVariable String id) {
        KubernetesCluster foundKubernetesCluster = kubernetesClusterRepository.findByUid(id);
        if(foundKubernetesCluster == null) {
            throw new ResourceNotFoundException("KubernetesCluster with id " + id + " not found");
        }
        kubernetesClusterRepository.delete(foundKubernetesCluster);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
