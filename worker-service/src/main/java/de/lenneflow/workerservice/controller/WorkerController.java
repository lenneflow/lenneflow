package de.lenneflow.workerservice.controller;

import de.lenneflow.workerservice.dto.*;
import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.enums.ClusterStatus;
import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.exception.ResourceNotFoundException;
import de.lenneflow.workerservice.component.CloudClusterController;
import de.lenneflow.workerservice.model.AccessToken;
import de.lenneflow.workerservice.model.KubernetesCluster;
import de.lenneflow.workerservice.model.CloudCredential;
import de.lenneflow.workerservice.repository.KubernetesClusterRepository;
import de.lenneflow.workerservice.repository.CloudCredentialRepository;
import de.lenneflow.workerservice.repository.AccessTokenRepository;
import de.lenneflow.workerservice.util.ObjectMapper;
import de.lenneflow.workerservice.util.Validator;
import de.lenneflow.workerservice.util.Util;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workers")
@Tag(name = "Workers API")
public class WorkerController {

    private static final String KUBERNETES_CLUSTER_NOT_FOUND = "KubernetesCluster not found";
    public static final String SERVICE_ACCOUNT_NAME = "lenneflow-sa";
    public static final String NAMESPACE = "lenneflow";
    public static final String INGRESS_NAME_SUFFIX = "-ingress";

    final Validator validator;
    final CloudClusterController cloudClusterController;
    final KubernetesClusterRepository kubernetesClusterRepository;
    final CloudCredentialRepository cloudCredentialRepository;
    final AccessTokenRepository accessTokenRepository;

    public WorkerController(Validator validator, KubernetesClusterRepository kubernetesClusterRepository, CloudClusterController cloudClusterController, CloudCredentialRepository cloudCredentialRepository, AccessTokenRepository accessTokenRepository) {
        this.validator = validator;
        this.kubernetesClusterRepository = kubernetesClusterRepository;
        this.cloudClusterController = cloudClusterController;
        this.accessTokenRepository = accessTokenRepository;
        this.cloudCredentialRepository = cloudCredentialRepository;
    }


    @Operation(summary = "Register an existing local Cluster")
    @PostMapping("/cluster/local/register")
    public KubernetesCluster registerLocalKubernetesCluster(@RequestBody LocalClusterDTO clusterDTO) {
        validator.validate(clusterDTO);
        KubernetesCluster kubernetesCluster = ObjectMapper.mapToKubernetesCluster(clusterDTO);
        kubernetesCluster.setUid(UUID.randomUUID().toString());
        kubernetesCluster.setCloudProvider(CloudProvider.LOCAL);
        kubernetesCluster.setIngressServiceName(kubernetesCluster.getClusterName().toLowerCase() + INGRESS_NAME_SUFFIX);
        kubernetesCluster.setStatus(ClusterStatus.REGISTRED);
        kubernetesCluster.setServiceUser(SERVICE_ACCOUNT_NAME);
        kubernetesCluster.setCreated(LocalDateTime.now());
        kubernetesCluster.setUpdated(LocalDateTime.now());
        validator.validate(kubernetesCluster, false);
        return kubernetesClusterRepository.save(kubernetesCluster);

    }

    @Operation(summary = "Register an existing cloud Cluster")
    @PostMapping("/cluster/cloud/register")
    public KubernetesCluster registerUnmanagedKubernetesCluster(@RequestBody UnmanagedClusterDTO clusterDTO) {
        validator.validate(clusterDTO);
        KubernetesCluster kubernetesCluster = ObjectMapper.mapToKubernetesCluster(clusterDTO);
        kubernetesCluster.setUid(UUID.randomUUID().toString());
        kubernetesCluster.setStatus(ClusterStatus.REGISTRED);
        kubernetesCluster.setIngressServiceName(kubernetesCluster.getClusterName().toLowerCase() + INGRESS_NAME_SUFFIX);
        kubernetesCluster.setServiceUser(SERVICE_ACCOUNT_NAME);
        kubernetesCluster.setCreated(LocalDateTime.now());
        kubernetesCluster.setUpdated(LocalDateTime.now());
        validator.validate(kubernetesCluster, false);
        return kubernetesClusterRepository.save(kubernetesCluster);

    }

    @Operation(summary = "Create a new cloud Cluster")
    @PostMapping("/cluster/cloud/create")
    public KubernetesCluster createManagedKubernetesCluster(@RequestBody ManagedClusterDTO clusterDTO) {

        validator.validate(clusterDTO);
        CloudCredential cloudCredential = cloudCredentialRepository.findByUid(clusterDTO.getCloudCredentialUid());

        //set hidden fields
        clusterDTO.setAccountId(cloudCredential.getAccountId());
        clusterDTO.setAccessKey(cloudCredential.getAccessKey());
        clusterDTO.setSecretKey(cloudCredential.getSecretKey());

        KubernetesCluster kubernetesCluster = ObjectMapper.mapToKubernetesCluster(clusterDTO);
        kubernetesCluster.setUid(UUID.randomUUID().toString());
        kubernetesCluster.setStatus(ClusterStatus.NEW);
        kubernetesCluster.setIngressServiceName(kubernetesCluster.getClusterName().toLowerCase() + INGRESS_NAME_SUFFIX);
        kubernetesCluster.setServiceUser(SERVICE_ACCOUNT_NAME);
        kubernetesCluster.setCreated(LocalDateTime.now());
        kubernetesCluster.setUpdated(LocalDateTime.now());

        validator.validate(kubernetesCluster, false);

        //Let k8s api creates the cluster
        HttpStatusCode status = cloudClusterController.createCluster(clusterDTO);
        if(status.value() != HttpStatus.OK.value() && status.value() != HttpStatus.CREATED.value()) {
            throw new InternalServiceException("Could not create the Kubernetes Cluster");
        }

        //Get current kubernetes object with status from k8s api.
        KubernetesCluster kubernetesClusterFromApi = cloudClusterController.getCluster(clusterDTO.getClusterName(), kubernetesCluster.getCloudProvider(), clusterDTO.getRegion());
        KubernetesCluster saved = updateKubernetesClusterWithDataFromApi(kubernetesCluster, kubernetesClusterFromApi);

        new Thread(() -> waitForCompleteCreation(saved, 25)).start();

        return saved;
    }

    @Operation(summary = "Create new cloud Credential")
    @PostMapping("/cloud/credentials/create")
    public CloudCredential createCloudClusterCredential(@RequestBody CloudCredentialDTO cloudCredentialDTO) {
        CloudCredential cloudCredential = ObjectMapper.mapToCloudCredential(cloudCredentialDTO);
        cloudCredential.setUid(UUID.randomUUID().toString());
        cloudCredential.setCreated(LocalDateTime.now());
        cloudCredential.setUpdated(LocalDateTime.now());
        return cloudCredentialRepository.save(cloudCredential);
    }

    @Operation(summary = "Get the list of cloud Credentials")
    @GetMapping("/cloud/credentials/list")
    public List<CloudCredential> getClusterCredentialList() {
        return cloudCredentialRepository.findAll();
    }

    @Operation(summary = "Delete the given cloud Credential")
    @DeleteMapping("/cloud/credentials/{uid}")
    public void deleteCloudCredential(@PathVariable String uid) {
        CloudCredential credential = cloudCredentialRepository.findByUid(uid);
        if(credential != null) {
            cloudCredentialRepository.delete(credential);
        }
    }

    @Operation(summary = "Find the given cloud Credential")
    @GetMapping("/cloud/credentials/{uid}")
    public CloudCredential getCloudCredential(@PathVariable String uid) {
        return cloudCredentialRepository.findByUid(uid);
    }

    @Operation(summary = "Create access Token")
    @PostMapping("/cluster/api-token/create")
    public AccessToken createLocalApiToken(@RequestBody AccessTokenDto accessTokenDto) {
        AccessToken accessToken = ObjectMapper.mapToAccessToken(accessTokenDto);
        accessToken.setUid(UUID.randomUUID().toString());
        accessToken.setUpdated(LocalDateTime.now());
        return accessTokenRepository.save(accessToken);
    }

    @Operation(summary = "Get access Token")
    @GetMapping("/cluster/api-token/{uid}")
    public AccessToken getLocalApiToken(@PathVariable String uid) {
        return accessTokenRepository.findByUid(uid);
    }

    @Operation(summary = "Get access Token")
    @DeleteMapping("/cluster/api-token/{uid}")
    public void deleteLocalApiToken(@PathVariable String uid) {
        accessTokenRepository.delete(accessTokenRepository.findByUid(uid));
    }

    @Operation(summary = "Get access Token list")
    @GetMapping("/cluster/api-token/list")
    public List<AccessToken> getLocalApiTokenList() {
        return accessTokenRepository.findAll();
    }



    @Operation(summary = "Update a node group")
    @PostMapping("/cluster/node-group/update")
    public KubernetesCluster updateNodeGroup(@RequestBody NodeGroupDTO nodeGroupDTO) {
        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(nodeGroupDTO.getClusterUid());
        if(kubernetesCluster != null){
            validator.validateThatManaged(kubernetesCluster);

            //set hidden fields
            nodeGroupDTO.setDesiredNodeCount(kubernetesCluster.getDesiredNodeCount());
            nodeGroupDTO.setCloudProvider(kubernetesCluster.getCloudProvider());
            nodeGroupDTO.setRegion(kubernetesCluster.getRegion());
            nodeGroupDTO.setClusterName(kubernetesCluster.getClusterName());

            validator.validate(nodeGroupDTO);

            //start updating the node group
            HttpStatusCode status = cloudClusterController.updateNodeGroup(nodeGroupDTO);
            if(status.value() != HttpStatus.OK.value() && status.value() != HttpStatus.CREATED.value()) {
                throw new InternalServiceException("Could not update the Kubernetes Cluster");
            }

            new Thread(() -> waitForCompleteCreation(kubernetesCluster, 20)).start();
            return kubernetesCluster;
        }
        throw new InternalServiceException("Could not find the Kubernetes Cluster to update");
    }

    @Operation(summary = "Get the list of all Clusters")
    @GetMapping("/cluster/list")
    public List<KubernetesCluster> getAllClusters()
    {
        return kubernetesClusterRepository.findAll();
    }

    @Operation(summary = "Update a Cluster")
    @PostMapping("/cluster/{uid}/update")
    public ResponseEntity<KubernetesCluster> updateWorker(@RequestBody UnmanagedClusterDTO unmanagedClusterDTO, @PathVariable String uid) {
        validator.validate(unmanagedClusterDTO);
        KubernetesCluster foundCluster = kubernetesClusterRepository.findByUid(uid);
        if(foundCluster == null) {
            throw new ResourceNotFoundException(KUBERNETES_CLUSTER_NOT_FOUND);
        }
        validator.validateThatUnmanaged(foundCluster);
        KubernetesCluster cluster =  ObjectMapper.mapToKubernetesCluster(unmanagedClusterDTO);
        cluster.setUid(foundCluster.getUid());
        cluster.setStatus(foundCluster.getStatus());
        cluster.setCreated(foundCluster.getCreated());
        cluster.setUpdated(LocalDateTime.now());
        validator.validate(cluster, true);
        KubernetesCluster savedKubernetesCluster = kubernetesClusterRepository.save(cluster);
        return new ResponseEntity<>(savedKubernetesCluster, HttpStatus.OK);
    }

    @Operation(summary = "Get a Cluster by UID")
    @GetMapping("/cluster/{uid}")
    public KubernetesCluster getWorker(@PathVariable String uid) {
        KubernetesCluster found = kubernetesClusterRepository.findByUid(uid);
        if(found == null) {
            throw new ResourceNotFoundException(KUBERNETES_CLUSTER_NOT_FOUND);
        }
        if(found.isManaged()){
            KubernetesCluster clusterFromApi = cloudClusterController.getCluster(found.getClusterName(), found.getCloudProvider(), found.getRegion());
            return updateKubernetesClusterWithDataFromApi(found, clusterFromApi);
        }
        return found;
    }

    @Operation(summary = "Delete a Cluster")
    @DeleteMapping("/cluster/{uid}")
    public void deleteWorker(@PathVariable String uid) {
        KubernetesCluster foundKubernetesCluster = kubernetesClusterRepository.findByUid(uid);
        if(foundKubernetesCluster == null) {
            throw new ResourceNotFoundException("KubernetesCluster with id " + uid + " not found");
        }
        if(foundKubernetesCluster.isManaged()){
            cloudClusterController.deleteAllResourcesInNamespace(foundKubernetesCluster, NAMESPACE);
            Util.pause(5000);
            HttpStatusCode status = cloudClusterController.deleteCluster(foundKubernetesCluster.getClusterName(), foundKubernetesCluster.getCloudProvider(), foundKubernetesCluster.getRegion());
            if(status.value() != HttpStatus.OK.value() && status.value() != HttpStatus.CREATED.value()) {
                throw new InternalServiceException("Could not delete the Kubernetes Cluster on the " + foundKubernetesCluster.getCloudProvider() + " cloud!");
            }
            new Thread(() -> waitForCompleteDeletion(foundKubernetesCluster)).start();
        }else{
            kubernetesClusterRepository.delete(foundKubernetesCluster);
        }
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
        kubernetesCluster.setHostUrl(kubernetesClusterFromApi.getHostUrl());
        kubernetesCluster.setMinimumNodeCount(kubernetesClusterFromApi.getMinimumNodeCount());
        kubernetesCluster.setMaximumNodeCount(kubernetesClusterFromApi.getMaximumNodeCount());
        kubernetesCluster.setDesiredNodeCount(kubernetesClusterFromApi.getDesiredNodeCount());
        kubernetesCluster.setUpdated(LocalDateTime.now());
        return kubernetesClusterRepository.save(kubernetesCluster);
    }

}
