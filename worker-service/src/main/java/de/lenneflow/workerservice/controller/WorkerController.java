package de.lenneflow.workerservice.controller;

import de.lenneflow.workerservice.dto.ManagedClusterDTO;
import de.lenneflow.workerservice.dto.UnmanagedClusterDTO;
import de.lenneflow.workerservice.dto.NodeGroupDTO;
import de.lenneflow.workerservice.enums.ClusterStatus;
import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.exception.PayloadNotValidException;
import de.lenneflow.workerservice.exception.ResourceNotFoundException;
import de.lenneflow.workerservice.component.CloudClusterController;
import de.lenneflow.workerservice.model.AccessToken;
import de.lenneflow.workerservice.model.KubernetesCluster;
import de.lenneflow.workerservice.model.CloudCredential;
import de.lenneflow.workerservice.repository.KubernetesClusterRepository;
import de.lenneflow.workerservice.repository.CloudCredentialRepository;
import de.lenneflow.workerservice.repository.AccessTokenRepository;
import de.lenneflow.workerservice.util.PayloadValidator;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    private static final String KUBERNETES_CLUSTER_NOT_FOUND = "KubernetesCluster not found";
    public static final String SERVICE_ACCOUNT_NAME = "lenneflow-sa";

    final PayloadValidator payloadValidator;
    final CloudClusterController cloudClusterController;
    final KubernetesClusterRepository kubernetesClusterRepository;
    final CloudCredentialRepository cloudCredentialRepository;
    final AccessTokenRepository accessTokenRepository;

    public WorkerController(PayloadValidator payloadValidator, KubernetesClusterRepository kubernetesClusterRepository, CloudClusterController cloudClusterController, CloudCredentialRepository cloudCredentialRepository, AccessTokenRepository accessTokenRepository) {
        this.payloadValidator = payloadValidator;
        this.kubernetesClusterRepository = kubernetesClusterRepository;
        this.cloudClusterController = cloudClusterController;
        this.accessTokenRepository = accessTokenRepository;
        this.cloudCredentialRepository = cloudCredentialRepository;
    }

    @PostMapping("/clusters/register")
    public ResponseEntity<KubernetesCluster> createLocalKubernetesCluster(@RequestBody UnmanagedClusterDTO clusterDTO) {

        payloadValidator.validate(clusterDTO);

        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setUid(UUID.randomUUID().toString());
        kubernetesCluster.setClusterName(clusterDTO.getClusterName());
        kubernetesCluster.setDescription(clusterDTO.getDescription());
        kubernetesCluster.setSupportedFunctionTypes(clusterDTO.getSupportedFunctionTypes());
        kubernetesCluster.setApiServerEndpoint(clusterDTO.getApiServerEndpoint());
        kubernetesCluster.setCaCertificate(clusterDTO.getCaCertificate());
        kubernetesCluster.setKubernetesAccessTokenUid(clusterDTO.getKubernetesAccessTokenUid());
        kubernetesCluster.setHostName(clusterDTO.getHostName());
        kubernetesCluster.setCloudProvider(clusterDTO.getCloudProvider());
        kubernetesCluster.setCloudCredentialUid(clusterDTO.getCloudCredentialUid());
        kubernetesCluster.setStatus(ClusterStatus.REGISTRED);
        kubernetesCluster.setManaged(false);
        kubernetesCluster.setIngressServiceName(kubernetesCluster.getClusterName().toLowerCase() + "-ingress");
        kubernetesCluster.setServiceUser(SERVICE_ACCOUNT_NAME);
        kubernetesCluster.setCreated(LocalDateTime.now());
        kubernetesCluster.setUpdated(LocalDateTime.now());

        payloadValidator.validate(kubernetesCluster);

        KubernetesCluster saved =  kubernetesClusterRepository.save(kubernetesCluster);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);

    }

    @PostMapping("/clusters/create")
    public ResponseEntity<KubernetesCluster> createCloudKubernetesCluster(@RequestBody ManagedClusterDTO clusterDTO) {

        payloadValidator.validate(clusterDTO);
        CloudCredential cloudCredential = cloudCredentialRepository.findByUid(clusterDTO.getCloudCredentialUid());

        //set hidden fields
        clusterDTO.setAccountId(cloudCredential.getAccountId());
        clusterDTO.setAccessKey(cloudCredential.getAccessKey());
        clusterDTO.setSecretKey(cloudCredential.getSecretKey());

        //create new kubernetes cluster
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setUid(UUID.randomUUID().toString());
        kubernetesCluster.setClusterName(clusterDTO.getClusterName());
        kubernetesCluster.setRegion(clusterDTO.getRegion());
        kubernetesCluster.setDescription(clusterDTO.getDescription());
        kubernetesCluster.setKubernetesVersion(clusterDTO.getKubernetesVersion());
        kubernetesCluster.setCloudProvider(clusterDTO.getCloudProvider());
        kubernetesCluster.setDesiredNodeCount(clusterDTO.getDesiredNodeCount());
        kubernetesCluster.setMinimumNodeCount(clusterDTO.getMinimumNodeCount());
        kubernetesCluster.setMaximumNodeCount(clusterDTO.getMaximumNodeCount());
        kubernetesCluster.setInstanceType(clusterDTO.getInstanceType());
        kubernetesCluster.setAmiType(clusterDTO.getAmiType());
        kubernetesCluster.setSupportedFunctionTypes(clusterDTO.getSupportedFunctionTypes());
        kubernetesCluster.setCloudCredentialUid(clusterDTO.getCloudCredentialUid());
        kubernetesCluster.setManaged(true);
        kubernetesCluster.setCreated(LocalDateTime.now());
        kubernetesCluster.setUpdated(LocalDateTime.now());

        //Let k8s api creates the cluster
        HttpStatusCode status = cloudClusterController.createCluster(clusterDTO);
        if(status.value() != HttpStatus.OK.value() && status.value() != HttpStatus.CREATED.value()) {
            throw new InternalServiceException("Could not create the Kubernetes Cluster");
        }

        //Get current kubernetes object with status from k8s api.
        KubernetesCluster kubernetesClusterFromApi = cloudClusterController.getCluster(clusterDTO.getClusterName(), clusterDTO.getCloudProvider(), clusterDTO.getRegion());
        updateKubernetesClusterWithDataFromApi(kubernetesCluster, kubernetesClusterFromApi);

        KubernetesCluster saved =  kubernetesClusterRepository.save(kubernetesCluster);

        new Thread(() -> waitForCompleteCreation(saved, 25)).start();

        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PostMapping("/clusters/credentials")
    public ResponseEntity<CloudCredential> createCloudClusterCredential(@RequestBody CloudCredential cloudCredential) {
        cloudCredential.setUid(UUID.randomUUID().toString());
        CloudCredential savedCredential = cloudCredentialRepository.save(cloudCredential);
        return new ResponseEntity<>(savedCredential, HttpStatus.OK);
    }

    @PostMapping("/clusters/node-group/update")
    public ResponseEntity<KubernetesCluster> updateNodeGroup(@RequestBody NodeGroupDTO nodeGroupDTO) {
        payloadValidator.validate(nodeGroupDTO);
        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(nodeGroupDTO.getClusterUid());
        if(kubernetesCluster != null){
            checkIfClusterIsManaged(kubernetesCluster, true);
            //set hidden fields
            nodeGroupDTO.setCloudProvider(kubernetesCluster.getCloudProvider());
            nodeGroupDTO.setRegion(kubernetesCluster.getRegion());
            nodeGroupDTO.setClusterName(kubernetesCluster.getClusterName());
            HttpStatusCode status = cloudClusterController.updateNodeGroup(nodeGroupDTO);
            if(status.value() != HttpStatus.OK.value() && status.value() != HttpStatus.CREATED.value()) {
                throw new InternalServiceException("Could not update the Kubernetes Cluster");
            }
            new Thread(() -> waitForCompleteCreation(kubernetesCluster, 20)).start();
            return new ResponseEntity<>(kubernetesCluster, HttpStatus.OK);
        }
        throw new InternalServiceException("Could not update the Kubernetes Cluster");
    }

    @GetMapping("/clusters")
    public List<KubernetesCluster> getAllClusters() {
        return kubernetesClusterRepository.findAll();
    }

    @PostMapping("/cluster/{uid}/update")
    public ResponseEntity<KubernetesCluster> updateWorker(@RequestBody KubernetesCluster kubernetesCluster, @PathVariable String uid) {
        KubernetesCluster foundCluster = kubernetesClusterRepository.findByUid(uid);
        if(foundCluster == null) {
            throw new ResourceNotFoundException(KUBERNETES_CLUSTER_NOT_FOUND);
        }
        checkIfClusterIsManaged(foundCluster, false);
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

    @DeleteMapping("/{id}")
    public void deleteWorker(@PathVariable String id) {
        KubernetesCluster foundKubernetesCluster = kubernetesClusterRepository.findByUid(id);
        if(foundKubernetesCluster == null) {
            throw new ResourceNotFoundException("KubernetesCluster with id " + id + " not found");
        }
        if(foundKubernetesCluster.isManaged()){
            HttpStatusCode status = cloudClusterController.deleteCluster(foundKubernetesCluster.getClusterName(), foundKubernetesCluster.getCloudProvider(), foundKubernetesCluster.getRegion());
            if(status.value() != HttpStatus.OK.value() && status.value() != HttpStatus.CREATED.value()) {
                throw new InternalServiceException("Could not delete the Kubernetes Cluster on the " + foundKubernetesCluster.getCloudProvider() + " cloud!");
            }
            waitForCompleteDeletion(foundKubernetesCluster);
        }
        kubernetesClusterRepository.delete(foundKubernetesCluster);
    }

    @Hidden
    @GetMapping(value={ "/check"})
    public String checkService() {
        return "Welcome to the Worker Service!";
    }

    @Hidden
    @PostMapping(value={ "/clusters/{uid}/update-used-ports"})
    public KubernetesCluster updateUsedPorts(@PathVariable("uid") String clusterUid, @RequestBody List<Integer> usedPorts) {
        KubernetesCluster cluster = kubernetesClusterRepository.findByUid(clusterUid);
        cluster.setUsedHostPorts(usedPorts);
        return kubernetesClusterRepository.save(cluster);
    }

    @Hidden
    @GetMapping(value={ "/clusters/{uid}/connection-token"})
    public AccessToken getConnectionToken(@PathVariable("uid") String clusterUid) {
        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(clusterUid);
        return cloudClusterController.getAccessToken(kubernetesCluster);
    }


    private void waitForCompleteCreation(KubernetesCluster kubernetesCluster, int timeOutInMinutes) {
        LocalDateTime start = LocalDateTime.now();
        while (true) {
            KubernetesCluster kubernetesClusterFromApi = cloudClusterController.getCluster(kubernetesCluster.getClusterName(), kubernetesCluster.getCloudProvider(), kubernetesCluster.getRegion());
            updateKubernetesClusterWithDataFromApi(kubernetesCluster, kubernetesClusterFromApi);
            if(kubernetesClusterFromApi.getStatus() == ClusterStatus.CREATED || start.plusMinutes(Math.max(timeOutInMinutes, 1)).isBefore(LocalDateTime.now())) {
                break;
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    private void waitForCompleteDeletion(KubernetesCluster kubernetesCluster) {
        LocalDateTime start = LocalDateTime.now();
        while (true) {
            KubernetesCluster kubernetesClusterFromApi = cloudClusterController.getCluster(kubernetesCluster.getClusterName(), kubernetesCluster.getCloudProvider(), kubernetesCluster.getRegion());
            if(kubernetesClusterFromApi == null) {
                kubernetesClusterRepository.delete(kubernetesCluster);
                break;
            }
            if(start.plusMinutes(25).isBefore(LocalDateTime.now())) {
                throw new InternalServiceException("The deletion of " + kubernetesCluster.getClusterName() + " failed!\nPlease check it manually");
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    private void updateKubernetesClusterWithDataFromApi(KubernetesCluster kubernetesCluster, KubernetesCluster kubernetesClusterFromApi) {
        kubernetesCluster.setApiServerEndpoint(kubernetesClusterFromApi.getApiServerEndpoint());
        kubernetesCluster.setCaCertificate(kubernetesClusterFromApi.getCaCertificate());
        kubernetesCluster.setStatus(kubernetesClusterFromApi.getStatus());
        kubernetesCluster.setUpdated(LocalDateTime.now());
    }

    private void checkIfClusterIsManaged(KubernetesCluster kubernetesCluster, boolean expectedToBeManaged) {
        if(kubernetesCluster.isManaged() != expectedToBeManaged){
            throw new PayloadNotValidException("The Kubernetes Cluster is not managed by Lenneflow. It is therefore not possible to make changes");
        }else{
            throw new PayloadNotValidException("The Kubernetes Cluster is managed by Lenneflow. This change is not available");
        }
    }
}
