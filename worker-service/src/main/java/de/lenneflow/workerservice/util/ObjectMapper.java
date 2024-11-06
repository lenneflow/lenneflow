package de.lenneflow.workerservice.util;

import de.lenneflow.workerservice.dto.*;
import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.model.AccessToken;
import de.lenneflow.workerservice.model.CloudCredential;
import de.lenneflow.workerservice.model.KubernetesCluster;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ObjectMapper {


    private ObjectMapper(){}

    public static KubernetesCluster mapToKubernetesCluster(ManagedClusterDTO clusterDTO) {
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setClusterName(clusterDTO.getClusterName());
        kubernetesCluster.setRegion(clusterDTO.getRegion());
        kubernetesCluster.setDescription(clusterDTO.getDescription());
        kubernetesCluster.setKubernetesVersion(clusterDTO.getKubernetesVersion());
        kubernetesCluster.setCloudProvider(CloudProvider.valueOf(clusterDTO.getCloudProvider().name()));
        kubernetesCluster.setDesiredNodeCount(clusterDTO.getDesiredNodeCount());
        kubernetesCluster.setMinimumNodeCount(clusterDTO.getMinimumNodeCount());
        kubernetesCluster.setMaximumNodeCount(clusterDTO.getMaximumNodeCount());
        kubernetesCluster.setInstanceType(clusterDTO.getInstanceType());
        kubernetesCluster.setAmiType(clusterDTO.getAmiType());
        kubernetesCluster.setSupportedFunctionTypes(clusterDTO.getSupportedFunctionTypes());
        kubernetesCluster.setCloudCredentialUid(clusterDTO.getCloudCredentialUid());
        kubernetesCluster.setManaged(true);
        return kubernetesCluster;
    }


    public static KubernetesCluster mapToKubernetesCluster(UnmanagedClusterDTO clusterDTO) {
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setClusterName(clusterDTO.getClusterName());
        kubernetesCluster.setRegion(clusterDTO.getRegion());
        kubernetesCluster.setDescription(clusterDTO.getDescription());
        kubernetesCluster.setSupportedFunctionTypes(clusterDTO.getSupportedFunctionTypes());
        kubernetesCluster.setApiServerEndpoint(clusterDTO.getApiServerEndpoint());
        kubernetesCluster.setCaCertificate(clusterDTO.getCaCertificate());
        kubernetesCluster.setCloudProvider(CloudProvider.valueOf(clusterDTO.getCloudProvider().name()));
        kubernetesCluster.setCloudCredentialUid(clusterDTO.getCloudCredentialUid());
        kubernetesCluster.setManaged(false);
        return kubernetesCluster;
    }

    public static KubernetesCluster mapToKubernetesCluster(LocalClusterDTO clusterDTO) {
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
        kubernetesCluster.setClusterName(clusterDTO.getClusterName());
        kubernetesCluster.setDescription(clusterDTO.getDescription());
        kubernetesCluster.setSupportedFunctionTypes(clusterDTO.getSupportedFunctionTypes());
        kubernetesCluster.setApiServerEndpoint(clusterDTO.getApiServerEndpoint());
        kubernetesCluster.setCaCertificate(clusterDTO.getCaCertificate());
        kubernetesCluster.setKubernetesAccessTokenUid(clusterDTO.getKubernetesAccessTokenUid());
        kubernetesCluster.setHostUrl(clusterDTO.getHostUrl().toLowerCase().startsWith("http")?clusterDTO.getHostUrl(): "http://"+clusterDTO.getHostUrl().toLowerCase());
        kubernetesCluster.setManaged(false);
        return kubernetesCluster;
    }


    public static AccessToken mapToAccessToken(AccessTokenDto accessTokenDto) {
        AccessToken accessToken = new AccessToken();
        accessToken.setToken(accessTokenDto.getToken());
        accessToken.setDescription(accessTokenDto.getDescription());
        if(accessTokenDto.getExpiration() == null || accessTokenDto.getExpiration().isBlank()){
            accessToken.setExpiration(LocalDateTime.now().plusYears(5));
        }else{
            try {
                LocalDateTime expiration = LocalDate.parse(accessTokenDto.getExpiration(), DateTimeFormatter.ofPattern("dd.MM.yyyy")).atStartOfDay();
                accessToken.setExpiration(expiration);
            }catch (Exception e){
                throw new InternalServiceException("Could not parse expiration date " + accessTokenDto.getExpiration());
            }

        }
        return accessToken;
    }

    public static CloudCredential mapToCloudCredential(CloudCredentialDTO cloudCredentialDTO){
        CloudCredential cloudCredential = new CloudCredential();
        cloudCredential.setName(cloudCredentialDTO.getName());
        cloudCredential.setDescription(cloudCredentialDTO.getDescription());
        cloudCredential.setAccessKey(cloudCredentialDTO.getAccessKey());
        cloudCredential.setSecretKey(cloudCredentialDTO.getSecretKey());
        cloudCredential.setAccountId(cloudCredentialDTO.getAccountId());
        return cloudCredential;
    }
}
