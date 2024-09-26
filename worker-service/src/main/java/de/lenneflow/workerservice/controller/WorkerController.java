package de.lenneflow.workerservice.controller;

import de.lenneflow.workerservice.dto.CloudClusterDTO;
import de.lenneflow.workerservice.dto.LocalClusterDTO;
import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.enums.ClusterStatus;
import de.lenneflow.workerservice.exception.ResourceNotFoundException;
import de.lenneflow.workerservice.kubernetes.KubernetesClusterController;
import de.lenneflow.workerservice.model.ApiCredential;
import de.lenneflow.workerservice.model.KubernetesCluster;
import de.lenneflow.workerservice.model.CloudCredential;
import de.lenneflow.workerservice.model.ClusterNodeGroup;
import de.lenneflow.workerservice.repository.ApiCredentialRepository;
import de.lenneflow.workerservice.repository.KubernetesClusterRepository;
import de.lenneflow.workerservice.repository.CloudCredentialRepository;
import de.lenneflow.workerservice.repository.ClusterNodeGroupRepository;
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
    public static final String NAMESPACE = "lenneflow";
    public static final String SERVICE_ACCOUNT_NAME = "lenneflow-sa";

    final ModelMapper modelMapper;
    final PayloadValidator payloadValidator;
    final KubernetesClusterController kubernetesClusterController;
    final KubernetesClusterRepository kubernetesClusterRepository;
    final CloudCredentialRepository localCredentialRepository;
    final CloudCredentialRepository cloudCredentialRepository;
    final ClusterNodeGroupRepository clusterNodeGroupRepository;
    final ApiCredentialRepository apiCredentialRepository;

    public WorkerController(PayloadValidator payloadValidator, KubernetesClusterRepository kubernetesClusterRepository, KubernetesClusterController kubernetesClusterController, CloudCredentialRepository localCredentialRepository, CloudCredentialRepository cloudCredentialRepository, ClusterNodeGroupRepository clusterNodeGroupRepository, ApiCredentialRepository apiCredentialRepository) {
        this.payloadValidator = payloadValidator;
        this.kubernetesClusterRepository = kubernetesClusterRepository;
        this.kubernetesClusterController = kubernetesClusterController;
        this.localCredentialRepository = localCredentialRepository;
        this.clusterNodeGroupRepository = clusterNodeGroupRepository;
        this.apiCredentialRepository = apiCredentialRepository;
        modelMapper = new ModelMapper();
        this.cloudCredentialRepository = cloudCredentialRepository;
    }

    @Hidden
    @GetMapping(value={ "/check"})
    public String checkService() {
        return "Welcome to the Worker Service!";
    }

    @Hidden
    @PostMapping(value={ "/clusters/{uid}/update-used-port"})
    public KubernetesCluster updateUsedPorts(@PathVariable("uid") String clusterUid, @RequestBody List<Integer> usedPorts) {
        KubernetesCluster cluster = kubernetesClusterRepository.findByUid(clusterUid);
        cluster.setUsedHostPorts(usedPorts);
        return kubernetesClusterRepository.save(cluster);
    }

    @Hidden
    @GetMapping(value={ "/clusters/api-credential/{uid}"})
    public ApiCredential getApiCredential(@PathVariable("uid") String apiCredentialUid) {
        return apiCredentialRepository.findByUid(apiCredentialUid);
    }

    @PostMapping("/clusters/local")
    public ResponseEntity<KubernetesCluster> createLocalKubernetesCluster(@RequestBody LocalClusterDTO clusterDTO) {
        KubernetesCluster kubernetesCluster = modelMapper.map(clusterDTO, KubernetesCluster.class);
        kubernetesCluster.setUid(UUID.randomUUID().toString());
        kubernetesCluster.setCreated(LocalDateTime.now());
        kubernetesCluster.setUpdated(LocalDateTime.now());
        kubernetesCluster.setCloudProvider(CloudProvider.LOCAL);
        kubernetesCluster.setStatus(ClusterStatus.CREATED);
        kubernetesCluster.setIngressServiceName(kubernetesCluster.getClusterName().toLowerCase() + "-ingress");
        kubernetesCluster.setServiceUser(SERVICE_ACCOUNT_NAME);
        kubernetesCluster.setCreated(LocalDateTime.now());
        kubernetesCluster.setUpdated(LocalDateTime.now());
        payloadValidator.validate(kubernetesCluster);


        ApiCredential apiCredential = new ApiCredential();
        apiCredential.setUid(UUID.randomUUID().toString());
        apiCredential.setDescription("Api Credential for cluster " + kubernetesCluster.getClusterName());
        apiCredential.setApiServerEndpoint(clusterDTO.getApiServerEndpoint());
        apiCredential.setApiAuthToken(clusterDTO.getApiAuthToken());
        apiCredential.setCreated(LocalDateTime.now());
        apiCredential.setUpdated(LocalDateTime.now());
        apiCredentialRepository.save(apiCredential);

        KubernetesCluster saved =  kubernetesClusterRepository.save(kubernetesCluster);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);

    }

    @PostMapping("/clusters/local")
    public ResponseEntity<KubernetesCluster> createCloudKubernetesCluster(@RequestBody CloudClusterDTO clusterDTO) {
        KubernetesCluster kubernetesCluster = modelMapper.map(clusterDTO, KubernetesCluster.class);
        kubernetesCluster.setUid(UUID.randomUUID().toString());
        kubernetesCluster.setCreated(LocalDateTime.now());
        kubernetesCluster.setUpdated(LocalDateTime.now());
        kubernetesCluster.setStatus(ClusterStatus.OFFLINE);
        kubernetesCluster.setIngressServiceName(kubernetesCluster.getClusterName().toLowerCase() + "-ingress");
        kubernetesCluster.setServiceUser(SERVICE_ACCOUNT_NAME);
        kubernetesCluster.setCreated(LocalDateTime.now());
        kubernetesCluster.setUpdated(LocalDateTime.now());
        payloadValidator.validate(kubernetesCluster);
        KubernetesCluster saved =  kubernetesClusterRepository.save(kubernetesCluster);

        if(kubernetesCluster.isCreate()){
            kubernetesCluster.setStatus(ClusterStatus.CREATING);
            kubernetesClusterRepository.save(kubernetesCluster);
            kubernetesClusterController.createCluster(kubernetesCluster);
        }else{
            kubernetesClusterController.getCluster(kubernetesCluster);
            kubernetesCluster.setStatus(ClusterStatus.CREATED);
            kubernetesClusterRepository.save(kubernetesCluster);
        }

        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PostMapping("/clusters/credentials")
    public ResponseEntity<CloudCredential> createCloudClusterCredential(@RequestBody CloudCredential cloudCredential) {
        cloudCredential.setUid(UUID.randomUUID().toString());
        CloudCredential savedCredential = cloudCredentialRepository.save(cloudCredential);
        return new ResponseEntity<>(savedCredential, HttpStatus.OK);
    }

    @PostMapping("/clusters/node-groups")
    public ResponseEntity<ClusterNodeGroup> createCloudClusterNodeGroup(@RequestBody ClusterNodeGroup clusterNodeGroup) {
        clusterNodeGroup.setUid(UUID.randomUUID().toString());
        ClusterNodeGroup savedNodeGroup = clusterNodeGroupRepository.save(clusterNodeGroup);
        if(savedNodeGroup.isCreate()){
            kubernetesClusterController.createNodeGroup(clusterNodeGroup);
        }
        return new ResponseEntity<>(savedNodeGroup, HttpStatus.OK);
    }

    @GetMapping("/clusters/{uid}/addons/{name}")
    public ResponseEntity<Object> createCloudClusterAddOn(@PathVariable("uid") String uid, @PathVariable("name") String addonName) {
        KubernetesCluster cluster = kubernetesClusterRepository.findByUid(uid);
        Object addon = kubernetesClusterController.createClusterAddOn(cluster, addonName);
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
