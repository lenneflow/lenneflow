package de.lenneflow.workerservice.util;

import de.lenneflow.workerservice.dto.CloudClusterDTO;
import de.lenneflow.workerservice.dto.LocalClusterDTO;
import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.exception.PayloadNotValidException;
import de.lenneflow.workerservice.model.KubernetesCluster;
import de.lenneflow.workerservice.repository.ApiCredentialRepository;
import de.lenneflow.workerservice.repository.KubernetesClusterRepository;
import org.springframework.stereotype.Component;

@Component
public class PayloadValidator {

    final KubernetesClusterRepository kubernetesClusterRepository;
    final ApiCredentialRepository apiCredentialRepository;


    public PayloadValidator(KubernetesClusterRepository kubernetesClusterRepository, ApiCredentialRepository apiCredentialRepository) {
        this.kubernetesClusterRepository = kubernetesClusterRepository;
        this.apiCredentialRepository = apiCredentialRepository;
    }


    public void validate(KubernetesCluster kubernetesCluster) {
        if(kubernetesCluster.getUid() == null || kubernetesCluster.getUid().isEmpty()) {
            throw new InternalServiceException("Uuid was not generated by the system");
        }

    }

    public void validate(CloudClusterDTO cloudClusterDTO) {

        if(cloudClusterDTO.getClusterName() == null || cloudClusterDTO.getClusterName().isEmpty()) {
            throw new PayloadNotValidException("KubernetesCluster Name is required");
        }
        if(cloudClusterDTO.getRegion() == null || cloudClusterDTO.getRegion().isEmpty()) {
            throw new PayloadNotValidException("Region is required");
        }

        if(cloudClusterDTO.getCloudProvider() == null || cloudClusterDTO.getCloudProvider().toString().isEmpty()) {
            throw new PayloadNotValidException("CloudProvider is required");
        }

        if(cloudClusterDTO.getSupportedFunctionTypes() == null || cloudClusterDTO.getSupportedFunctionTypes().isEmpty()) {
            throw new PayloadNotValidException("SupportedFunctionTypes is required");
        }
        if(cloudClusterDTO.isCreate()){
            if(cloudClusterDTO.getSecurityGroupId() == null || cloudClusterDTO.getSecurityGroupId().isEmpty()) {
                throw new PayloadNotValidException("Security Group Id is required");
            }
            if(cloudClusterDTO.getSubnetIds() == null || cloudClusterDTO.getSubnetIds().isEmpty()) {
                throw new PayloadNotValidException("Subnet Ids are required");
            }
            if(cloudClusterDTO.getRoleArn() == null || cloudClusterDTO.getRoleArn().isEmpty()) {
                throw new PayloadNotValidException("Role Arn is required");
            }
        }


    }

    public void validate(LocalClusterDTO localClusterDTO) {
        if(localClusterDTO.getClusterName() == null || localClusterDTO.getClusterName().isEmpty()) {
            throw new PayloadNotValidException("KubernetesCluster Name is required");
        }
        if(localClusterDTO.getHostName() == null || localClusterDTO.getHostName().isEmpty()) {
            throw new PayloadNotValidException("HostName is required");
        }
        if(localClusterDTO.getApiServerEndpoint() == null || localClusterDTO.getApiServerEndpoint().isEmpty()) {
            throw new PayloadNotValidException("ApiServerEndpoint is required");
        }
        if(localClusterDTO.getApiAuthToken() == null || localClusterDTO.getApiAuthToken().isEmpty()) {
            throw new PayloadNotValidException("ApiAuthToken is required");
        }
        if(localClusterDTO.getSupportedFunctionTypes() == null || localClusterDTO.getSupportedFunctionTypes().isEmpty()) {
            throw new PayloadNotValidException("SupportedFunctionTypes is required");
        }
    }
}
