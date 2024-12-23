package de.lenneflow.workerservice.util;

import de.lenneflow.workerservice.dto.*;
import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.exception.PayloadNotValidException;
import de.lenneflow.workerservice.model.KubernetesCluster;
import de.lenneflow.workerservice.repository.AccessTokenRepository;
import de.lenneflow.workerservice.repository.CloudCredentialRepository;
import de.lenneflow.workerservice.repository.KubernetesClusterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidatorTest {

    @Mock
    private KubernetesClusterRepository kubernetesClusterRepository;

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @Mock
    private CloudCredentialRepository cloudCredentialRepository;

    private Validator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new Validator(kubernetesClusterRepository, accessTokenRepository, cloudCredentialRepository);
    }

    @Test
    void validateKubernetesCluster_shouldThrowExceptionWhenUidIsNull() {
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setUid(null);

        assertThrows(InternalServiceException.class, () -> validator.validate(kubernetesCluster, false));
    }

    @Test
    void validateKubernetesCluster_shouldThrowExceptionWhenClusterExists() {
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setUid("uid");
        kubernetesCluster.setClusterName("clusterName");
        kubernetesCluster.setCloudProvider(CloudProvider.AWS);
        kubernetesCluster.setRegion("us-east-1");

        when(kubernetesClusterRepository.findByClusterNameAndCloudProviderAndRegion("clusterName", CloudProvider.AWS, "us-east-1")).thenReturn(kubernetesCluster);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(kubernetesCluster, false));
    }

    @Test
    void validateNodeGroupDTO_shouldThrowExceptionWhenDesiredNodeCountIsLessThanOne() {
        NodeGroupDTO nodeGroupDTO = new NodeGroupDTO();
        nodeGroupDTO.setDesiredNodeCount(0);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(nodeGroupDTO));
    }

    @Test
    void validateNodeGroupDTO_shouldThrowExceptionWhenClusterUidIsInvalid() {
        NodeGroupDTO nodeGroupDTO = new NodeGroupDTO();
        nodeGroupDTO.setClusterUid("invalidUid");

        when(kubernetesClusterRepository.findByUid("invalidUid")).thenReturn(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(nodeGroupDTO));
    }

    @Test
    void validateManagedClusterDTO_shouldThrowExceptionWhenClusterNameIsNull() {
        ManagedClusterDTO managedClusterDTO = new ManagedClusterDTO();
        managedClusterDTO.setClusterName(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(managedClusterDTO));
    }

    @Test
    void validateManagedClusterDTO_shouldThrowExceptionWhenCloudCredentialUidIsInvalid() {
        ManagedClusterDTO managedClusterDTO = new ManagedClusterDTO();
        managedClusterDTO.setCloudCredentialUid("invalidUid");

        when(cloudCredentialRepository.findByUid("invalidUid")).thenReturn(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(managedClusterDTO));
    }

    @Test
    void validateUnmanagedClusterDTO_shouldThrowExceptionWhenClusterNameIsNull() {
        UnmanagedClusterDTO unmanagedClusterDTO = new UnmanagedClusterDTO();
        unmanagedClusterDTO.setClusterName(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(unmanagedClusterDTO));
    }

    @Test
    void validateUnmanagedClusterDTO_shouldThrowExceptionWhenCloudCredentialUidIsInvalid() {
        UnmanagedClusterDTO unmanagedClusterDTO = new UnmanagedClusterDTO();
        unmanagedClusterDTO.setCloudCredentialUid("invalidUid");

        when(cloudCredentialRepository.findByUid("invalidUid")).thenReturn(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(unmanagedClusterDTO));
    }

    @Test
    void validateLocalClusterDTO_shouldThrowExceptionWhenClusterNameIsNull() {
        LocalClusterDTO localClusterDTO = new LocalClusterDTO();
        localClusterDTO.setClusterName(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(localClusterDTO));
    }

    @Test
    void validateLocalClusterDTO_shouldThrowExceptionWhenAccessTokenUidIsInvalid() {
        LocalClusterDTO localClusterDTO = new LocalClusterDTO();
        localClusterDTO.setKubernetesAccessTokenUid("invalidUid");

        when(accessTokenRepository.findByUid("invalidUid")).thenReturn(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(localClusterDTO));
    }

    @Test
    void validateThatManaged_shouldThrowExceptionWhenClusterIsNotManaged() {
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setManaged(false);

        assertThrows(PayloadNotValidException.class, () -> validator.validateThatManaged(kubernetesCluster));
    }

    @Test
    void validateThatUnmanaged_shouldThrowExceptionWhenClusterIsManaged() {
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setManaged(true);

        assertThrows(PayloadNotValidException.class, () -> validator.validateThatUnmanaged(kubernetesCluster));
    }
}