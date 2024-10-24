package de.lenneflow.workerservice.util;

import de.lenneflow.workerservice.dto.ManagedClusterDTO;
import de.lenneflow.workerservice.dto.UnmanagedClusterDTO;
import de.lenneflow.workerservice.enums.ClusterStatus;
import de.lenneflow.workerservice.model.KubernetesCluster;

import java.time.LocalDateTime;
import java.util.UUID;

public class ObjectMapper {



    public static KubernetesCluster mapToKubernetesCluster(ManagedClusterDTO clusterDTO) {
        KubernetesCluster kubernetesCluster = new KubernetesCluster();
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
        kubernetesCluster.setKubernetesAccessTokenUid(clusterDTO.getKubernetesAccessTokenUid());
        kubernetesCluster.setHostAddress(clusterDTO.getHostName());
        kubernetesCluster.setCloudProvider(clusterDTO.getCloudProvider());
        kubernetesCluster.setCloudCredentialUid(clusterDTO.getCloudCredentialUid());
        kubernetesCluster.setManaged(false);
        return kubernetesCluster;
    }
}
