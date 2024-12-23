package de.lenneflow.workerservice.controller;

import de.lenneflow.workerservice.dto.*;
import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.enums.ClusterStatus;
import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.exception.ResourceNotFoundException;
import de.lenneflow.workerservice.model.AccessToken;
import de.lenneflow.workerservice.model.KubernetesCluster;
import de.lenneflow.workerservice.model.CloudCredential;
import de.lenneflow.workerservice.repository.KubernetesClusterRepository;
import de.lenneflow.workerservice.repository.CloudCredentialRepository;
import de.lenneflow.workerservice.repository.AccessTokenRepository;
import de.lenneflow.workerservice.util.ObjectMapper;
import de.lenneflow.workerservice.util.Validator;
import de.lenneflow.workerservice.component.CloudClusterController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkerControllerTest {

    @Mock
    private Validator validator;

    @Mock
    private KubernetesClusterRepository kubernetesClusterRepository;

    @Mock
    private CloudClusterController cloudClusterController;

    @Mock
    private CloudCredentialRepository cloudCredentialRepository;

    @Mock
    private AccessTokenRepository accessTokenRepository;

    private WorkerController workerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workerController = new WorkerController(validator, kubernetesClusterRepository, cloudClusterController, cloudCredentialRepository, accessTokenRepository);
    }

    @Test
    void registerLocalKubernetesCluster_shouldRegisterCluster() {
        LocalClusterDTO clusterDTO = new LocalClusterDTO();
        clusterDTO.setClusterName("clusterName");
        clusterDTO.setHostUrl("hostUrl");
        clusterDTO.setApiServerEndpoint("apiServerEndpoint");
        clusterDTO.setKubernetesAccessTokenUid("uid");
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setClusterName("clusterName");
        when(kubernetesClusterRepository.save(any(KubernetesCluster.class))).thenReturn(kubernetesCluster);

        KubernetesCluster result = workerController.registerLocalKubernetesCluster(clusterDTO);

        assertNotNull(result);
        verify(validator).validate(clusterDTO);
        verify(kubernetesClusterRepository).save(any(KubernetesCluster.class));
    }

    @Test
    void registerUnmanagedKubernetesCluster_shouldRegisterCluster() {
        UnmanagedClusterDTO clusterDTO = new UnmanagedClusterDTO();
        clusterDTO.setClusterName("clusterName");
        clusterDTO.setCloudProvider(CloudProviderDto.AWS);
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setClusterName("clusterName");
        kubernetesCluster.setCloudProvider(CloudProvider.AWS);
        when(kubernetesClusterRepository.save(any(KubernetesCluster.class))).thenReturn(kubernetesCluster);

        KubernetesCluster result = workerController.registerUnmanagedKubernetesCluster(clusterDTO);

        assertNotNull(result);
        verify(validator).validate(clusterDTO);
        verify(kubernetesClusterRepository).save(any(KubernetesCluster.class));
    }

    @Test
    void createManagedKubernetesCluster_shouldCreateCluster() {
        ManagedClusterDTO clusterDTO = new ManagedClusterDTO();
        clusterDTO.setClusterName("clusterName");
        clusterDTO.setRegion("region");
        clusterDTO.setCloudCredentialUid("uid");
        CloudCredential cloudCredential = new CloudCredential();

        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setCloudProvider(CloudProvider.AWS);
        kubernetesCluster.setRegion("region");
        kubernetesCluster.setStatus(ClusterStatus.CREATED);
        kubernetesCluster.setClusterName("clusterName");
        kubernetesCluster.setUid("uid");

        when(cloudCredentialRepository.findByUid(anyString())).thenReturn(cloudCredential, cloudCredential);
        when(cloudClusterController.createCluster(any(ManagedClusterDTO.class))).thenReturn(HttpStatus.OK);
        when(cloudClusterController.getCluster(anyString(), any(CloudProvider.class), anyString())).thenReturn(kubernetesCluster, kubernetesCluster);
        when(kubernetesClusterRepository.save(any(KubernetesCluster.class))).thenReturn(kubernetesCluster, kubernetesCluster);

        try (MockedStatic<ObjectMapper> objectMapperMockedStatic = Mockito.mockStatic(ObjectMapper.class)){
            objectMapperMockedStatic.when(() ->ObjectMapper.mapToKubernetesCluster((ManagedClusterDTO) any())).thenReturn(kubernetesCluster, kubernetesCluster);
            KubernetesCluster result = workerController.createManagedKubernetesCluster(clusterDTO);
            assertNotNull(result);
            verify(validator).validate(clusterDTO);
            verify(kubernetesClusterRepository).save(any(KubernetesCluster.class));
        }
    }

    @Test
    void createManagedKubernetesCluster_shouldThrowExceptionWhenClusterCreationFails() {
        ManagedClusterDTO clusterDTO = new ManagedClusterDTO();
        clusterDTO.setCloudCredentialUid("uid");
        clusterDTO.setCloudProvider(CloudProviderDto.AWS);
        CloudCredential cloudCredential = new CloudCredential();
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setClusterName("clusterName");
        when(cloudCredentialRepository.findByUid(anyString())).thenReturn(cloudCredential);
        when(cloudClusterController.createCluster(any(ManagedClusterDTO.class))).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        try (MockedStatic<ObjectMapper> objectMapperMockedStatic = Mockito.mockStatic(ObjectMapper.class)){
            objectMapperMockedStatic.when(() ->ObjectMapper.mapToKubernetesCluster((ManagedClusterDTO) any())).thenReturn(kubernetesCluster);
            assertThrows(InternalServiceException.class, () -> workerController.createManagedKubernetesCluster(clusterDTO));
        }
    }

    @Test
    void createCloudClusterCredential_shouldCreateCredential() {
        CloudCredentialDTO cloudCredentialDTO = new CloudCredentialDTO();
        CloudCredential cloudCredential = new CloudCredential();
        when(cloudCredentialRepository.save(any(CloudCredential.class))).thenReturn(cloudCredential);

        CloudCredential result = workerController.createCloudClusterCredential(cloudCredentialDTO);

        assertNotNull(result);
        verify(cloudCredentialRepository).save(any(CloudCredential.class));
    }

    @Test
    void createLocalApiToken_shouldCreateToken() {
        AccessTokenDto accessTokenDto = new AccessTokenDto();
        AccessToken accessToken = new AccessToken();
        when(accessTokenRepository.save(any(AccessToken.class))).thenReturn(accessToken);

        AccessToken result = workerController.createLocalApiToken(accessTokenDto);

        assertNotNull(result);
        verify(accessTokenRepository).save(any(AccessToken.class));
    }

    @Test
    void getLocalApiToken_shouldReturnToken() {
        AccessToken accessToken = new AccessToken();
        when(accessTokenRepository.findByUid(anyString())).thenReturn(accessToken);

        AccessToken result = workerController.getLocalApiToken("uid");

        assertNotNull(result);
        verify(accessTokenRepository).findByUid("uid");
    }

    @Test
    void deleteLocalApiToken_shouldDeleteToken() {
        AccessToken accessToken = new AccessToken();
        when(accessTokenRepository.findByUid(anyString())).thenReturn(accessToken);

        workerController.deleteLocalApiToken("uid");

        verify(accessTokenRepository).delete(accessToken);
    }

    @Test
    void getLocalApiTokenList_shouldReturnTokenList() {
        List<AccessToken> tokens = List.of(new AccessToken(), new AccessToken());
        when(accessTokenRepository.findAll()).thenReturn(tokens);

        List<AccessToken> result = workerController.getLocalApiTokenList();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(accessTokenRepository).findAll();
    }

    @Test
    void updateNodeGroup_shouldUpdateNodeGroup() {
        NodeGroupDTO nodeGroupDTO = new NodeGroupDTO();
        nodeGroupDTO.setClusterUid("uid");
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setClusterName("clusterName");
        when(kubernetesClusterRepository.findByUid(anyString())).thenReturn(kubernetesCluster);
        when(cloudClusterController.updateNodeGroup(any(NodeGroupDTO.class))).thenReturn(HttpStatus.OK);

        KubernetesCluster result = workerController.updateNodeGroup(nodeGroupDTO);

        assertNotNull(result);
        verify(kubernetesClusterRepository).findByUid(nodeGroupDTO.getClusterUid());
        verify(cloudClusterController).updateNodeGroup(nodeGroupDTO);
    }

    @Test
    void updateNodeGroup_shouldThrowExceptionWhenClusterNotFound() {
        NodeGroupDTO nodeGroupDTO = new NodeGroupDTO();
        when(kubernetesClusterRepository.findByUid(anyString())).thenReturn(null);

        assertThrows(InternalServiceException.class, () -> workerController.updateNodeGroup(nodeGroupDTO));
    }

    @Test
    void getAllClusters_shouldReturnClusterList() {
        List<KubernetesCluster> clusters = List.of(new KubernetesCluster(), new KubernetesCluster());
        when(kubernetesClusterRepository.findAll()).thenReturn(clusters);

        List<KubernetesCluster> result = workerController.getAllClusters();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(kubernetesClusterRepository).findAll();
    }

    @Test
    void updateWorker_shouldUpdateCluster() {
        UnmanagedClusterDTO unmanagedClusterDTO = new UnmanagedClusterDTO();
        unmanagedClusterDTO.setCloudProvider(CloudProviderDto.AWS);
        KubernetesCluster foundCluster = new KubernetesCluster();
        when(kubernetesClusterRepository.findByUid(anyString())).thenReturn(foundCluster);
        when(kubernetesClusterRepository.save(any(KubernetesCluster.class))).thenReturn(new KubernetesCluster());

        ResponseEntity<KubernetesCluster> result = workerController.updateWorker(unmanagedClusterDTO, "uid");

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(kubernetesClusterRepository).findByUid("uid");
        verify(kubernetesClusterRepository).save(any(KubernetesCluster.class));
    }

    @Test
    void updateWorker_shouldThrowExceptionWhenClusterNotFound() {
        UnmanagedClusterDTO unmanagedClusterDTO = new UnmanagedClusterDTO();
        when(kubernetesClusterRepository.findByUid(anyString())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> workerController.updateWorker(unmanagedClusterDTO, "uid"));
    }

    @Test
    void getWorker_shouldReturnCluster() {
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        when(kubernetesClusterRepository.findByUid(anyString())).thenReturn(kubernetesCluster);

        KubernetesCluster result = workerController.getWorker("uid");

        assertNotNull(result);
        verify(kubernetesClusterRepository).findByUid("uid");
    }

    @Test
    void getWorker_shouldThrowExceptionWhenClusterNotFound() {
        when(kubernetesClusterRepository.findByUid(anyString())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> workerController.getWorker("uid"));
    }

    @Test
    void deleteWorker_shouldDeleteCluster() {
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        when(kubernetesClusterRepository.findByUid(anyString())).thenReturn(kubernetesCluster);

        workerController.deleteWorker("uid");

        verify(kubernetesClusterRepository).delete(kubernetesCluster);
    }

    @Test
    void deleteWorker_shouldThrowExceptionWhenClusterNotFound() {
        when(kubernetesClusterRepository.findByUid(anyString())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> workerController.deleteWorker("uid"));
    }
}