package de.lenneflow.workerservice.kubernetes.cloud;

import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.model.KubernetesCluster;
import de.lenneflow.workerservice.model.CloudNodeGroup;
import de.lenneflow.workerservice.repository.KubernetesClusterRepository;
import org.springframework.stereotype.Component;

@Component
public class CloudController {

    private static final String UNSUPPORTED_CLOUD_PROVIDER = "Unsupported Cloud Provider";


    private final KubernetesClusterRepository kubernetesClusterRepository;
    private final AWSController awsController;
    private final GoogleController googleController;
    private final AzureController azureController;

    public CloudController(KubernetesClusterRepository kubernetesClusterRepository, AWSController awsController, GoogleController googleController, AzureController azureController) {
        this.kubernetesClusterRepository = kubernetesClusterRepository;
        this.awsController = awsController;
        this.googleController = googleController;
        this.azureController = azureController;
    }

    public Object createCluster(KubernetesCluster kubernetesCluster) {
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return googleController.createCluster(kubernetesCluster);
            case AMAZON_AWS:
                return awsController.createCluster(kubernetesCluster);
            case MICROSOFT_AZURE:
                return azureController.createCluster(kubernetesCluster);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }

    public Object createClusterAddOn(KubernetesCluster kubernetesCluster, String addOnName) {
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return googleController.createClusterAddOn(kubernetesCluster, addOnName);
            case AMAZON_AWS:
                return awsController.createClusterAddOn(kubernetesCluster, addOnName);
            case MICROSOFT_AZURE:
                return azureController.createClusterAddOn(kubernetesCluster, addOnName);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }

    public Object createNodeGroup(CloudNodeGroup cloudNodeGroup) {

        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(cloudNodeGroup.getClusterUid());

        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return googleController.createNodeGroup(cloudNodeGroup);
            case AMAZON_AWS:
                return awsController.createNodeGroup(cloudNodeGroup);
            case MICROSOFT_AZURE:
                return azureController.createNodeGroup(cloudNodeGroup);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }

    public Object getCluster(KubernetesCluster kubernetesCluster) {
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return googleController.getCluster(kubernetesCluster);
            case AMAZON_AWS:
                return awsController.getCluster(kubernetesCluster);
            case MICROSOFT_AZURE:
                return azureController.getCluster(kubernetesCluster);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }

    public Object getNodeGroup(CloudNodeGroup cloudNodeGroup) {
        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(cloudNodeGroup.getClusterUid());
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return googleController.getNodeGroup(cloudNodeGroup);
            case AMAZON_AWS:
                return awsController.getNodeGroup(cloudNodeGroup);
            case MICROSOFT_AZURE:
                return azureController.getNodeGroup(cloudNodeGroup);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }

}
