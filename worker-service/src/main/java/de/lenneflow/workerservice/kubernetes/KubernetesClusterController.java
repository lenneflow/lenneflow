package de.lenneflow.workerservice.kubernetes;

import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.kubernetes.cloudproviders.AWSClusterController;
import de.lenneflow.workerservice.kubernetes.cloudproviders.AzureClusterController;
import de.lenneflow.workerservice.kubernetes.cloudproviders.GoogleClusterController;
import de.lenneflow.workerservice.model.KubernetesCluster;
import de.lenneflow.workerservice.model.ClusterNodeGroup;
import de.lenneflow.workerservice.repository.KubernetesClusterRepository;
import org.springframework.stereotype.Component;

@Component
public class KubernetesClusterController implements IClusterController {

    private static final String UNSUPPORTED_CLOUD_PROVIDER = "Unsupported Cloud Provider";

    private final KubernetesClusterRepository kubernetesClusterRepository;
    private final AWSClusterController awsClusterController;
    private final GoogleClusterController googleClusterController;
    private final AzureClusterController azureClusterController;

    public KubernetesClusterController(KubernetesClusterRepository kubernetesClusterRepository, AWSClusterController awsClusterController, GoogleClusterController googleClusterController, AzureClusterController azureClusterController) {
        this.kubernetesClusterRepository = kubernetesClusterRepository;
        this.awsClusterController = awsClusterController;
        this.googleClusterController = googleClusterController;
        this.azureClusterController = azureClusterController;
    }

    public Object createCluster(KubernetesCluster kubernetesCluster) {
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return googleClusterController.createCluster(kubernetesCluster);
            case AMAZON_AWS:
                return awsClusterController.createCluster(kubernetesCluster);
            case MICROSOFT_AZURE:
                return azureClusterController.createCluster(kubernetesCluster);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }

    public Object createClusterAddOn(KubernetesCluster kubernetesCluster, String addOnName) {
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return googleClusterController.createClusterAddOn(kubernetesCluster, addOnName);
            case AMAZON_AWS:
                return awsClusterController.createClusterAddOn(kubernetesCluster, addOnName);
            case MICROSOFT_AZURE:
                return azureClusterController.createClusterAddOn(kubernetesCluster, addOnName);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }

    public Object createNodeGroup(ClusterNodeGroup clusterNodeGroup) {

        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(clusterNodeGroup.getClusterUid());

        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return googleClusterController.createNodeGroup(clusterNodeGroup);
            case AMAZON_AWS:
                return awsClusterController.createNodeGroup(clusterNodeGroup);
            case MICROSOFT_AZURE:
                return azureClusterController.createNodeGroup(clusterNodeGroup);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }

    @Override
    public String getSessionToken(KubernetesCluster kubernetesCluster) {
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return googleClusterController.getSessionToken(kubernetesCluster);
            case AMAZON_AWS:
                return awsClusterController.getSessionToken(kubernetesCluster);
            case MICROSOFT_AZURE:
                return azureClusterController.getSessionToken(kubernetesCluster);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }


    public Object getCluster(KubernetesCluster kubernetesCluster) {
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return googleClusterController.getCluster(kubernetesCluster);
            case AMAZON_AWS:
                return awsClusterController.getCluster(kubernetesCluster);
            case MICROSOFT_AZURE:
                return azureClusterController.getCluster(kubernetesCluster);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }

    public Object getNodeGroup(ClusterNodeGroup clusterNodeGroup) {
        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(clusterNodeGroup.getClusterUid());
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return googleClusterController.getNodeGroup(clusterNodeGroup);
            case AMAZON_AWS:
                return awsClusterController.getNodeGroup(clusterNodeGroup);
            case MICROSOFT_AZURE:
                return azureClusterController.getNodeGroup(clusterNodeGroup);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }



}
