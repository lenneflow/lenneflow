package de.lenneflow.workerservice.util;

import de.lenneflow.workerservice.dto.*;
import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.model.AccessToken;
import de.lenneflow.workerservice.model.CloudCredential;
import de.lenneflow.workerservice.model.KubernetesCluster;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObjectMapperTest {

    @Test
    void mapToKubernetesCluster_shouldMapManagedClusterDTOToKubernetesCluster() {
        ManagedClusterDTO dto = new ManagedClusterDTO();
        dto.setClusterName("testCluster");
        dto.setRegion("us-east-1");
        dto.setDescription("Test Description");
        dto.setKubernetesVersion("1.18");
        dto.setCloudProvider(CloudProviderDto.AWS);
        dto.setDesiredNodeCount(3);
        dto.setMinimumNodeCount(1);
        dto.setMaximumNodeCount(5);
        dto.setInstanceType("t2.medium");
        dto.setAmiType("ami-123456");
        dto.setSupportedFunctionTypes(List.of("functionType1"));
        dto.setCloudCredentialUid("credentialUid");

        KubernetesCluster result = ObjectMapper.mapToKubernetesCluster(dto);

        assertNotNull(result);
        assertEquals("testCluster", result.getClusterName());
        assertEquals("us-east-1", result.getRegion());
        assertEquals("Test Description", result.getDescription());
        assertEquals("1.18", result.getKubernetesVersion());
        assertEquals(CloudProvider.AWS, result.getCloudProvider());
        assertEquals(3, result.getDesiredNodeCount());
        assertEquals(1, result.getMinimumNodeCount());
        assertEquals(5, result.getMaximumNodeCount());
        assertEquals("t2.medium", result.getInstanceType());
        assertEquals("ami-123456", result.getAmiType());
        assertEquals(List.of("functionType1"), result.getSupportedFunctionTypes());
        assertEquals("credentialUid", result.getCloudCredentialUid());
        assertTrue(result.isManaged());
    }

    @Test
    void mapToKubernetesCluster_shouldMapUnmanagedClusterDTOToKubernetesCluster() {
        UnmanagedClusterDTO dto = new UnmanagedClusterDTO();
        dto.setClusterName("testCluster");
        dto.setRegion("us-east-1");
        dto.setDescription("Test Description");
        dto.setSupportedFunctionTypes(List.of("functionType1"));
        dto.setApiServerEndpoint("https://api.server");
        dto.setCaCertificate("caCert");
        dto.setCloudProvider(CloudProviderDto.AWS);
        dto.setCloudCredentialUid("credentialUid");

        KubernetesCluster result = ObjectMapper.mapToKubernetesCluster(dto);

        assertNotNull(result);
        assertEquals("testCluster", result.getClusterName());
        assertEquals("us-east-1", result.getRegion());
        assertEquals("Test Description", result.getDescription());
        assertEquals(List.of("functionType1"), result.getSupportedFunctionTypes());
        assertEquals("https://api.server", result.getApiServerEndpoint());
        assertEquals("caCert", result.getCaCertificate());
        assertEquals(CloudProvider.AWS, result.getCloudProvider());
        assertEquals("credentialUid", result.getCloudCredentialUid());
        assertFalse(result.isManaged());
    }

    @Test
    void mapToKubernetesCluster_shouldMapLocalClusterDTOToKubernetesCluster() {
        LocalClusterDTO dto = new LocalClusterDTO();
        dto.setClusterName("testCluster");
        dto.setDescription("Test Description");
        dto.setSupportedFunctionTypes(List.of("functionType1"));
        dto.setApiServerEndpoint("https://api.server");
        dto.setCaCertificate("caCert");
        dto.setKubernetesAccessTokenUid("tokenUid");
        dto.setHostUrl("localhost");

        KubernetesCluster result = ObjectMapper.mapToKubernetesCluster(dto);

        assertNotNull(result);
        assertEquals("testCluster", result.getClusterName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(List.of("functionType1"), result.getSupportedFunctionTypes());
        assertEquals("https://api.server", result.getApiServerEndpoint());
        assertEquals("caCert", result.getCaCertificate());
        assertEquals("tokenUid", result.getKubernetesAccessTokenUid());
        assertEquals("http://localhost", result.getHostUrl());
        assertFalse(result.isManaged());
    }

    @Test
    void mapToAccessToken_shouldMapAccessTokenDtoToAccessToken() {
        AccessTokenDto dto = new AccessTokenDto();
        dto.setToken("testToken");
        dto.setDescription("Test Description");
        dto.setExpiration("01.01.2025");

        AccessToken result = ObjectMapper.mapToAccessToken(dto);

        assertNotNull(result);
        assertEquals("testToken", result.getToken());
        assertEquals("Test Description", result.getDescription());
        assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0), result.getExpiration());
    }

    @Test
    void mapToAccessToken_shouldThrowExceptionWhenExpirationDateIsInvalid() {
        AccessTokenDto dto = new AccessTokenDto();
        dto.setToken("testToken");
        dto.setDescription("Test Description");
        dto.setExpiration("invalidDate");

        assertThrows(InternalServiceException.class, () -> ObjectMapper.mapToAccessToken(dto));
    }

    @Test
    void mapToCloudCredential_shouldMapCloudCredentialDTOToCloudCredential() {
        CloudCredentialDTO dto = new CloudCredentialDTO();
        dto.setName("testName");
        dto.setDescription("Test Description");
        dto.setAccessKey("accessKey");
        dto.setSecretKey("secretKey");
        dto.setAccountId("accountId");

        CloudCredential result = ObjectMapper.mapToCloudCredential(dto);

        assertNotNull(result);
        assertEquals("testName", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals("accessKey", result.getAccessKey());
        assertEquals("secretKey", result.getSecretKey());
        assertEquals("accountId", result.getAccountId());
    }
}