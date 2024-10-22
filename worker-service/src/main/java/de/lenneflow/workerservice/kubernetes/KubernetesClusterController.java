package de.lenneflow.workerservice.kubernetes;

import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.kubernetes.cloudproviders.AWSClusterController;
import de.lenneflow.workerservice.kubernetes.cloudproviders.AzureClusterController;
import de.lenneflow.workerservice.kubernetes.cloudproviders.GoogleClusterController;
import de.lenneflow.workerservice.model.AccessToken;
import de.lenneflow.workerservice.model.KubernetesCluster;
import de.lenneflow.workerservice.model.ClusterNodeGroup;
import de.lenneflow.workerservice.repository.KubernetesClusterRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Component
public class KubernetesClusterController implements IClusterController {

    private static final String UNSUPPORTED_CLOUD_PROVIDER = "Unsupported Cloud Provider";

    @Value("${k8sapi.address}")
    private String k8sApiRootEndpoint;

    private final RestTemplate restTemplate;
    private final KubernetesClusterRepository kubernetesClusterRepository;
    private final AWSClusterController awsClusterController;
    private final GoogleClusterController googleClusterController;
    private final AzureClusterController azureClusterController;

    public KubernetesClusterController(RestTemplate restTemplate, KubernetesClusterRepository kubernetesClusterRepository, AWSClusterController awsClusterController, GoogleClusterController googleClusterController, AzureClusterController azureClusterController) {
        this.restTemplate = restTemplate;
        this.kubernetesClusterRepository = kubernetesClusterRepository;
        this.awsClusterController = awsClusterController;
        this.googleClusterController = googleClusterController;
        this.azureClusterController = azureClusterController;
    }

    public AccessToken getAccessToken(KubernetesCluster kubernetesCluster) {
        String accessTokenUrl = k8sApiRootEndpoint + "/access-token/provider/"+ kubernetesCluster.getCloudProvider().toString() + "/cluster/" + kubernetesCluster.getClusterName() + "/region/" +kubernetesCluster.getRegion();
        return restTemplate.getForObject(accessTokenUrl, AccessToken.class);
    }

    public Object createCluster(KubernetesCluster kubernetesCluster) {
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE:
                return googleClusterController.createCluster(kubernetesCluster);
            case AMAZON:
                return awsClusterController.createCluster(kubernetesCluster);
            case MICROSOFT:
                return azureClusterController.createCluster(kubernetesCluster);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }

    public Object createClusterAddOn(KubernetesCluster kubernetesCluster, String addOnName) {
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE:
                return googleClusterController.createClusterAddOn(kubernetesCluster, addOnName);
            case AMAZON:
                return awsClusterController.createClusterAddOn(kubernetesCluster, addOnName);
            case MICROSOFT:
                return azureClusterController.createClusterAddOn(kubernetesCluster, addOnName);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }

    public Object createNodeGroup(ClusterNodeGroup clusterNodeGroup) {

        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(clusterNodeGroup.getClusterUid());

        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE:
                return googleClusterController.createNodeGroup(clusterNodeGroup);
            case AMAZON:
                return awsClusterController.createNodeGroup(clusterNodeGroup);
            case MICROSOFT:
                return azureClusterController.createNodeGroup(clusterNodeGroup);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }

    @Override
    public String getSessionToken(KubernetesCluster kubernetesCluster, Date expirationDate) {
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE:
                return googleClusterController.getSessionToken(kubernetesCluster, expirationDate);
            case AMAZON:
                return awsClusterController.getSessionToken(kubernetesCluster, expirationDate);
            case MICROSOFT:
                return azureClusterController.getSessionToken(kubernetesCluster, expirationDate);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }

    @Override
    public String getApiServerEndpoint(KubernetesCluster kubernetesCluster) {
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE:
                return googleClusterController.getApiServerEndpoint(kubernetesCluster);
            case AMAZON:
                return awsClusterController.getApiServerEndpoint(kubernetesCluster);
            case MICROSOFT:
                return azureClusterController.getApiServerEndpoint(kubernetesCluster);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }


    public Object getCluster(KubernetesCluster kubernetesCluster) {
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE:
                return googleClusterController.getCluster(kubernetesCluster);
            case AMAZON:
                return awsClusterController.getCluster(kubernetesCluster);
            case MICROSOFT:
                return azureClusterController.getCluster(kubernetesCluster);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }

    public Object getNodeGroup(ClusterNodeGroup clusterNodeGroup) {
        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(clusterNodeGroup.getClusterUid());
        switch (kubernetesCluster.getCloudProvider()){
            case GOOGLE:
                return googleClusterController.getNodeGroup(clusterNodeGroup);
            case AMAZON:
                return awsClusterController.getNodeGroup(clusterNodeGroup);
            case MICROSOFT:
                return azureClusterController.getNodeGroup(clusterNodeGroup);
            default:
                throw new InternalServiceException(UNSUPPORTED_CLOUD_PROVIDER);
        }
    }



}
