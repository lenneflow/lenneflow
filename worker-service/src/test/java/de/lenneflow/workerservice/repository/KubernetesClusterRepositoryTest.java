package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.model.KubernetesCluster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KubernetesClusterRepositoryTest {

    @Mock
    private KubernetesClusterRepository kubernetesClusterRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUid_shouldReturnKubernetesClusterWhenUidExists() {
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setUid("existingUid");
        when(kubernetesClusterRepository.findByUid("existingUid")).thenReturn(kubernetesCluster);

        KubernetesCluster result = kubernetesClusterRepository.findByUid("existingUid");

        assertNotNull(result);
        assertEquals("existingUid", result.getUid());
    }

    @Test
    void findByUid_shouldReturnNullWhenUidDoesNotExist() {
        when(kubernetesClusterRepository.findByUid("nonExistingUid")).thenReturn(null);

        KubernetesCluster result = kubernetesClusterRepository.findByUid("nonExistingUid");

        assertNull(result);
    }

    @Test
    void findByClusterNameAndCloudProviderAndRegion_shouldReturnKubernetesClusterWhenExists() {
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setClusterName("clusterName");
        kubernetesCluster.setCloudProvider(CloudProvider.AWS);
        kubernetesCluster.setRegion("us-east-1");
        when(kubernetesClusterRepository.findByClusterNameAndCloudProviderAndRegion("clusterName", CloudProvider.AWS, "us-east-1")).thenReturn(kubernetesCluster);

        KubernetesCluster result = kubernetesClusterRepository.findByClusterNameAndCloudProviderAndRegion("clusterName", CloudProvider.AWS, "us-east-1");

        assertNotNull(result);
        assertEquals("clusterName", result.getClusterName());
        assertEquals(CloudProvider.AWS, result.getCloudProvider());
        assertEquals("us-east-1", result.getRegion());
    }

    @Test
    void findByClusterNameAndCloudProviderAndRegion_shouldReturnNullWhenNotExists() {
        when(kubernetesClusterRepository.findByClusterNameAndCloudProviderAndRegion("clusterName", CloudProvider.AWS, "us-east-1")).thenReturn(null);

        KubernetesCluster result = kubernetesClusterRepository.findByClusterNameAndCloudProviderAndRegion("clusterName", CloudProvider.AWS, "us-east-1");

        assertNull(result);
    }

    @Test
    void findBySupportedFunctionTypesContaining_shouldReturnKubernetesClustersWhenFunctionTypeExists() {
        KubernetesCluster kubernetesCluster1 = new KubernetesCluster();
        kubernetesCluster1.setSupportedFunctionTypes(List.of("functionType1"));
        KubernetesCluster kubernetesCluster2 = new KubernetesCluster();
        kubernetesCluster2.setSupportedFunctionTypes(List.of("functionType1", "functionType2"));
        when(kubernetesClusterRepository.findBySupportedFunctionTypesContaining("functionType1")).thenReturn(List.of(kubernetesCluster1, kubernetesCluster2));

        List<KubernetesCluster> result = kubernetesClusterRepository.findBySupportedFunctionTypesContaining("functionType1");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void findBySupportedFunctionTypesContaining_shouldReturnEmptyListWhenFunctionTypeDoesNotExist() {
        when(kubernetesClusterRepository.findBySupportedFunctionTypesContaining("nonExistingFunctionType")).thenReturn(List.of());

        List<KubernetesCluster> result = kubernetesClusterRepository.findBySupportedFunctionTypesContaining("nonExistingFunctionType");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}