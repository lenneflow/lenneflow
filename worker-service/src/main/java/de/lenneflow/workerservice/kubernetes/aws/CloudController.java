package de.lenneflow.workerservice.kubernetes.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.retry.RetryPolicy;
import com.amazonaws.services.eks.AmazonEKS;
import com.amazonaws.services.eks.AmazonEKSClientBuilder;
import com.amazonaws.services.eks.model.*;
import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.exception.ResourceNotFoundException;
import de.lenneflow.workerservice.model.CloudCluster;
import de.lenneflow.workerservice.model.CloudCredential;
import de.lenneflow.workerservice.model.CloudNodeGroup;
import de.lenneflow.workerservice.repository.CloudClusterRepository;
import de.lenneflow.workerservice.repository.CloudCredentialRepository;
import org.springframework.stereotype.Component;

import static com.amazonaws.retry.PredefinedRetryPolicies.DEFAULT_BACKOFF_STRATEGY;
import static com.amazonaws.retry.PredefinedRetryPolicies.DEFAULT_MAX_ERROR_RETRY;

@Component
public class CloudController {

    private final CloudCredentialRepository cloudCredentialRepository;
    private final CloudClusterRepository cloudClusterRepository;
    private final AWSController awsController;

    public CloudController(CloudCredentialRepository cloudCredentialRepository, CloudClusterRepository cloudClusterRepository, AWSController awsController) {
        this.cloudCredentialRepository = cloudCredentialRepository;
        this.cloudClusterRepository = cloudClusterRepository;
        this.awsController = awsController;
    }

    public Cluster createCluster(CloudCluster cloudCluster) {
        switch (cloudCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return null;
            case AMAZON_AWS:
                return awsController.createCluster(cloudCluster);
            case MICROSOFT_AZURE:
                return null;
            default:
                throw new InternalServiceException("Unsupported Cloud Provider");
        }
    }

    public Addon createClusterAddOn(CloudCluster cloudCluster, String addOnName) {
        switch (cloudCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return null;
            case AMAZON_AWS:
                return awsController.createClusterAddOn(cloudCluster, addOnName);
            case MICROSOFT_AZURE:
                return null;
            default:
                throw new InternalServiceException("Unsupported Cloud Provider");
        }
    }

    public Nodegroup createNodeGroup(CloudNodeGroup cloudNodeGroup) {

        CloudCluster cloudCluster = cloudClusterRepository.findByUid(cloudNodeGroup.getClusterUid());
        AmazonEKS eksClient = getClient(cloudCluster);

        switch (cloudCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return null;
            case AMAZON_AWS:
                return awsController.createNodeGroup(cloudNodeGroup);
            case MICROSOFT_AZURE:
                return null;
            default:
                throw new InternalServiceException("Unsupported Cloud Provider");
        }
    }

    public Cluster getCluster(CloudCluster cloudCluster) {
        AmazonEKS eksClient = getClient(cloudCluster);
        switch (cloudCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return null;
            case AMAZON_AWS:
                return awsController.getCluster(cloudCluster);
            case MICROSOFT_AZURE:
                return null;
            default:
                throw new InternalServiceException("Unsupported Cloud Provider");
        }
    }

    public Nodegroup getNodeGroup(CloudNodeGroup cloudNodeGroup) {
        CloudCluster cloudCluster = cloudClusterRepository.findByUid(cloudNodeGroup.getClusterUid());
        switch (cloudCluster.getCloudProvider()){
            case GOOGLE_CLOUD:
                return null;
            case AMAZON_AWS:
                return awsController.getNodeGroup(cloudNodeGroup);
            case MICROSOFT_AZURE:
                return null;
            default:
                throw new InternalServiceException("Unsupported Cloud Provider");
        }
    }

    private AmazonEKS getClient(CloudCluster cloudCluster) {
        CloudCredential cloudCredential = cloudCredentialRepository.findByUid(cloudCluster.getCloudCredentialUid());
        AWSCredentials credentials = new BasicAWSCredentials(cloudCredential.getAccessKey(), cloudCredential.getSecretKey());
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTPS);
        clientConfig.setMaxErrorRetry(DEFAULT_MAX_ERROR_RETRY);
        clientConfig.setRetryPolicy(new RetryPolicy(PredefinedRetryPolicies.DEFAULT_RETRY_CONDITION,
                DEFAULT_BACKOFF_STRATEGY, DEFAULT_MAX_ERROR_RETRY, false));

        return AmazonEKSClientBuilder.standard()
                .withClientConfiguration(clientConfig)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(cloudCluster.getRegion())
                .build();
    }
}
