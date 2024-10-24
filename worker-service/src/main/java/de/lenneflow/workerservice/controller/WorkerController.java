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
import de.lenneflow.workerservice.util.ObjectMapper;
import de.lenneflow.workerservice.util.PayloadValidator;
import de.lenneflow.workerservice.util.Util;
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

    @PostMapping("/cluster/register")
    public ResponseEntity<KubernetesCluster> createLocalKubernetesCluster(@RequestBody UnmanagedClusterDTO clusterDTO) {
        payloadValidator.validate(clusterDTO);
        KubernetesCluster kubernetesCluster = ObjectMapper.mapToKubernetesCluster(clusterDTO);
        kubernetesCluster.setUid(UUID.randomUUID().toString());
        kubernetesCluster.setStatus(ClusterStatus.REGISTRED);
        kubernetesCluster.setIngressServiceName(kubernetesCluster.getClusterName().toLowerCase() + "-ingress");
        kubernetesCluster.setServiceUser(SERVICE_ACCOUNT_NAME);
        kubernetesCluster.setCreated(LocalDateTime.now());
        kubernetesCluster.setUpdated(LocalDateTime.now());

        payloadValidator.validate(kubernetesCluster);

        KubernetesCluster saved =  kubernetesClusterRepository.save(kubernetesCluster);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);

    }

    @PostMapping("/cluster/create")
    public ResponseEntity<KubernetesCluster> createCloudKubernetesCluster(@RequestBody ManagedClusterDTO clusterDTO) {

        payloadValidator.validate(clusterDTO);
        CloudCredential cloudCredential = cloudCredentialRepository.findByUid(clusterDTO.getCloudCredentialUid());

        //set hidden fields
        clusterDTO.setAccountId(cloudCredential.getAccountId());
        clusterDTO.setAccessKey(cloudCredential.getAccessKey());
        clusterDTO.setSecretKey(cloudCredential.getSecretKey());

        KubernetesCluster kubernetesCluster = ObjectMapper.mapToKubernetesCluster(clusterDTO);
        kubernetesCluster.setUid(UUID.randomUUID().toString());
        kubernetesCluster.setStatus(ClusterStatus.NEW);
        kubernetesCluster.setCreated(LocalDateTime.now());
        kubernetesCluster.setUpdated(LocalDateTime.now());

        //Let k8s api creates the cluster
        HttpStatusCode status = cloudClusterController.createCluster(clusterDTO);
        if(status.value() != HttpStatus.OK.value() && status.value() != HttpStatus.CREATED.value()) {
            throw new InternalServiceException("Could not create the Kubernetes Cluster");
        }

        //Get current kubernetes object with status from k8s api.
        KubernetesCluster kubernetesClusterFromApi = cloudClusterController.getCluster(clusterDTO.getClusterName(), clusterDTO.getCloudProvider(), clusterDTO.getRegion());
        KubernetesCluster saved = updateKubernetesClusterWithDataFromApi(kubernetesCluster, kubernetesClusterFromApi);

        new Thread(() -> waitForCompleteCreation(saved, 25)).start();

        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @PostMapping("/cloud-provider/credentials")
    public ResponseEntity<CloudCredential> createCloudClusterCredential(@RequestBody CloudCredential cloudCredential) {
        cloudCredential.setUid(UUID.randomUUID().toString());
        CloudCredential savedCredential = cloudCredentialRepository.save(cloudCredential);
        return new ResponseEntity<>(savedCredential, HttpStatus.OK);
    }

    @PostMapping("/cluster/node-group/update")
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
    public List<KubernetesCluster> getAllClusters()
    {
        return kubernetesClusterRepository.findAll();
    }

    @PostMapping("/cluster/{uid}/update")
    public ResponseEntity<KubernetesCluster> updateWorker(@RequestBody UnmanagedClusterDTO unmanagedClusterDTO, @PathVariable String uid) {
        payloadValidator.validate(unmanagedClusterDTO);
        KubernetesCluster foundCluster = kubernetesClusterRepository.findByUid(uid);
        if(foundCluster == null) {
            throw new ResourceNotFoundException(KUBERNETES_CLUSTER_NOT_FOUND);
        }
        checkIfClusterIsManaged(foundCluster, false);
        KubernetesCluster cluster =  ObjectMapper.mapToKubernetesCluster(unmanagedClusterDTO);
        cluster.setUid(foundCluster.getUid());
        cluster.setStatus(foundCluster.getStatus());
        cluster.setCreated(foundCluster.getCreated());
        cluster.setUpdated(LocalDateTime.now());
        payloadValidator.validate(cluster);
        KubernetesCluster savedKubernetesCluster = kubernetesClusterRepository.save(cluster);
        return new ResponseEntity<>(savedKubernetesCluster, HttpStatus.OK);
    }

    @GetMapping("/cluster/{uid}")
    public ResponseEntity<KubernetesCluster> getWorker(@PathVariable String uid) {
        KubernetesCluster found = kubernetesClusterRepository.findByUid(uid);
        if(found == null) {
            throw new ResourceNotFoundException(KUBERNETES_CLUSTER_NOT_FOUND);
        }
        if(found.isManaged()){
            KubernetesCluster clusterFromApi = cloudClusterController.getCluster(found.getClusterName(), found.getCloudProvider(), found.getRegion());
            updateKubernetesClusterWithDataFromApi(found, clusterFromApi);
        }
        return new ResponseEntity<>(found, HttpStatus.OK);
    }

    @DeleteMapping("/cluster/{uid}")
    public void deleteWorker(@PathVariable String uid) {
        KubernetesCluster foundKubernetesCluster = kubernetesClusterRepository.findByUid(uid);
        if(foundKubernetesCluster == null) {
            throw new ResourceNotFoundException("KubernetesCluster with id " + uid + " not found");
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
    @PostMapping(value={ "/cluster/{uid}/update-used-ports"})
    public KubernetesCluster updateUsedPorts(@PathVariable("uid") String clusterUid, @RequestBody List<Integer> usedPorts) {
        KubernetesCluster cluster = kubernetesClusterRepository.findByUid(clusterUid);
        cluster.setUsedHostPorts(usedPorts);
        return kubernetesClusterRepository.save(cluster);
    }

    @Hidden
    @GetMapping(value={ "/cluster/{uid}/connection-token"})
    public AccessToken getConnectionToken(@PathVariable("uid") String clusterUid) {
        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(clusterUid);

        if(kubernetesCluster.isManaged()) {
            return cloudClusterController.getConnectionToken(kubernetesCluster.getClusterName(), kubernetesCluster.getCloudProvider(), kubernetesCluster.getRegion());
        }
        if(kubernetesCluster.getKubernetesAccessTokenUid() == null || kubernetesCluster.getKubernetesAccessTokenUid().isEmpty()) {
            throw new InternalServiceException("Cluster does not have access token ID. Impossible to get the access token!");
        }
        AccessToken currentToken = accessTokenRepository.findByUid(kubernetesCluster.getKubernetesAccessTokenUid());
        if(currentToken == null) {
            throw new InternalServiceException("The token ID of the Cluster is not correct! No access token found!");
        }
        if(currentToken.getExpiration().isBefore(LocalDateTime.now())) {
            throw new InternalServiceException("The access token is expired. Please consider creating a new access token for the Cluster " + kubernetesCluster.getClusterName());
        }
        return currentToken;
    }

    private void waitForCompleteCreation(KubernetesCluster kubernetesCluster, int timeOutInMinutes) {
        LocalDateTime start = LocalDateTime.now();
        while (true) {
            KubernetesCluster kubernetesClusterFromApi = cloudClusterController.getCluster(kubernetesCluster.getClusterName(), kubernetesCluster.getCloudProvider(), kubernetesCluster.getRegion());
            updateKubernetesClusterWithDataFromApi(kubernetesCluster, kubernetesClusterFromApi);
            if(kubernetesClusterFromApi.getStatus() == ClusterStatus.CREATED || start.plusMinutes(Math.max(timeOutInMinutes, 1)).isBefore(LocalDateTime.now())) {
                break;
            }
            Util.pause(60000);
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
            Util.pause(60000);
        }
    }

    private KubernetesCluster updateKubernetesClusterWithDataFromApi(KubernetesCluster kubernetesCluster, KubernetesCluster kubernetesClusterFromApi) {
        kubernetesCluster.setApiServerEndpoint(kubernetesClusterFromApi.getApiServerEndpoint());
        kubernetesCluster.setCaCertificate(kubernetesClusterFromApi.getCaCertificate());
        kubernetesCluster.setStatus(kubernetesClusterFromApi.getStatus());
        kubernetesCluster.setUpdated(LocalDateTime.now());
        return kubernetesClusterRepository.save(kubernetesCluster);
    }

    private void checkIfClusterIsManaged(KubernetesCluster kubernetesCluster, boolean expectedToBeManaged) {
        if(kubernetesCluster.isManaged() != expectedToBeManaged){
            throw new PayloadNotValidException("The Kubernetes Cluster is not managed by Lenneflow. It is therefore not possible to make changes");
        }else{
            throw new PayloadNotValidException("The Kubernetes Cluster is managed by Lenneflow. This change is not available");
        }
    }

}
