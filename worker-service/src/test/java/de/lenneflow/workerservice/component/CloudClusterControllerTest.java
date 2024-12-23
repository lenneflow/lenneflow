package de.lenneflow.workerservice.component;

import de.lenneflow.workerservice.dto.ManagedClusterDTO;
import de.lenneflow.workerservice.dto.NodeGroupDTO;
import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.model.AccessToken;
import de.lenneflow.workerservice.model.KubernetesCluster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CloudClusterControllerTest {

    @Mock
    private RestTemplate restTemplate;

    private CloudClusterController cloudClusterController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cloudClusterController = new CloudClusterController(restTemplate);
    }

    @Test
    void createCluster_shouldReturnHttpStatusOk() {
        ManagedClusterDTO clusterDTO = new ManagedClusterDTO();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        HttpStatusCode result = cloudClusterController.createCluster(clusterDTO);

        assertEquals(HttpStatus.OK, result);
    }

    @Test
    void createCluster_shouldReturnHttpStatusInternalServerError() {
        ManagedClusterDTO clusterDTO = new ManagedClusterDTO();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        HttpStatusCode result = cloudClusterController.createCluster(clusterDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
    }

    @Test
    void getCluster_shouldReturnKubernetesCluster() {
        KubernetesCluster expectedCluster = new KubernetesCluster();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(KubernetesCluster.class)))
                .thenReturn(new ResponseEntity<>(expectedCluster, HttpStatus.OK));

        KubernetesCluster result = cloudClusterController.getCluster("clusterName", CloudProvider.AWS, "region");

        assertNotNull(result);
        assertEquals(expectedCluster, result);
    }

    @Test
    void getCluster_shouldReturnNullWhenNotFound() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(KubernetesCluster.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        KubernetesCluster result = cloudClusterController.getCluster("clusterName", CloudProvider.AWS, "region");

        assertNull(result);
    }

    @Test
    void getConnectionToken_shouldReturnAccessToken() {
        AccessToken expectedToken = new AccessToken();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(AccessToken.class)))
                .thenReturn(new ResponseEntity<>(expectedToken, HttpStatus.OK));

        AccessToken result = cloudClusterController.getConnectionToken("clusterName", CloudProvider.AWS, "region");

        assertNotNull(result);
        assertEquals(expectedToken, result);
    }

    @Test
    void getConnectionToken_shouldReturnNullWhenNotFound() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(AccessToken.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        AccessToken result = cloudClusterController.getConnectionToken("clusterName", CloudProvider.AWS, "region");

        assertNull(result);
    }

    @Test
    void updateNodeGroup_shouldReturnHttpStatusOk() {
        NodeGroupDTO nodeGroupDTO = new NodeGroupDTO();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        HttpStatusCode result = cloudClusterController.updateNodeGroup(nodeGroupDTO);

        assertEquals(HttpStatus.OK, result);
    }

    @Test
    void updateNodeGroup_shouldReturnHttpStatusInternalServerError() {
        NodeGroupDTO nodeGroupDTO = new NodeGroupDTO();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        HttpStatusCode result = cloudClusterController.updateNodeGroup(nodeGroupDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
    }

    @Test
    void deleteCluster_shouldReturnHttpStatusOk() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), isNull(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        HttpStatusCode result = cloudClusterController.deleteCluster("clusterName", CloudProvider.AWS, "region");

        assertEquals(HttpStatus.OK, result);
    }

    @Test
    void deleteCluster_shouldReturnHttpStatusNotFound() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), isNull(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        HttpStatusCode result = cloudClusterController.deleteCluster("clusterName", CloudProvider.AWS, "region");

        assertEquals(HttpStatus.NOT_FOUND, result);
    }

}