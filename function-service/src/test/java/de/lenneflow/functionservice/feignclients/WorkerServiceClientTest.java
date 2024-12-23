package de.lenneflow.functionservice.feignclients;

import de.lenneflow.functionservice.feignmodels.AccessToken;
import de.lenneflow.functionservice.feignmodels.KubernetesCluster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class WorkerServiceClientTest {

    @Mock
    private WorkerServiceClient workerServiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getKubernetesClusterById_shouldReturnClusterWhenIdExists() {
        KubernetesCluster cluster = new KubernetesCluster();
        cluster.setUid("existingUid");
        when(workerServiceClient.getKubernetesClusterById("existingUid")).thenReturn(cluster);

        KubernetesCluster result = workerServiceClient.getKubernetesClusterById("existingUid");

        assertNotNull(result);
        assertEquals("existingUid", result.getUid());
    }

    @Test
    void getKubernetesClusterList_shouldReturnListOfClusters() {
        KubernetesCluster cluster1 = new KubernetesCluster();
        KubernetesCluster cluster2 = new KubernetesCluster();
        when(workerServiceClient.getKubernetesClusterList()).thenReturn(List.of(cluster1, cluster2));

        List<KubernetesCluster> result = workerServiceClient.getKubernetesClusterList();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void updateUsedPorts_shouldReturnUpdatedCluster() {
        KubernetesCluster cluster = new KubernetesCluster();
        cluster.setUid("existingUid");
        List<Integer> usedPorts = List.of(8080, 8081);
        when(workerServiceClient.updateUsedPorts("existingUid", usedPorts)).thenReturn(cluster);

        KubernetesCluster result = workerServiceClient.updateUsedPorts("existingUid", usedPorts);

        assertNotNull(result);
        assertEquals("existingUid", result.getUid());
    }

    @Test
    void getK8sConnectionToken_shouldReturnAccessToken() {
        AccessToken accessToken = new AccessToken();
        accessToken.setToken("token");
        when(workerServiceClient.getK8sConnectionToken("existingUid")).thenReturn(accessToken);

        AccessToken result = workerServiceClient.getK8sConnectionToken("existingUid");

        assertNotNull(result);
        assertEquals("token", result.getToken());
    }

    @Test
    void ping_shouldReturnPong() {
        when(workerServiceClient.ping()).thenReturn("pong");

        String result = workerServiceClient.ping();

        assertEquals("pong", result);
    }
}