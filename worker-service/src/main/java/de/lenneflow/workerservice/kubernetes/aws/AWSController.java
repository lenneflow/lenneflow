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
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.amazonaws.retry.PredefinedRetryPolicies.DEFAULT_BACKOFF_STRATEGY;
import static com.amazonaws.retry.PredefinedRetryPolicies.DEFAULT_MAX_ERROR_RETRY;

@Component
public class AWSController {

    private final List<String> addOnList;
    private final CloudCredentialRepository cloudCredentialRepository;
    private final CloudClusterRepository cloudClusterRepository;

    public AWSController(CloudCredentialRepository cloudCredentialRepository, CloudClusterRepository cloudClusterRepository) {
        this.cloudCredentialRepository = cloudCredentialRepository;
        this.cloudClusterRepository = cloudClusterRepository;
        addOnList = Arrays.asList("kube-proxy", "vpc-cni", "eks-pod-identity-agent", "coredns");
    }

    public Cluster createCluster(CloudCluster cloudCluster) {
        AmazonEKS eksClient = getClient(cloudCluster);
        if (eksClient.listClusters(new ListClustersRequest()).getClusters().contains(cloudCluster.getClusterName())) {
            throw new InternalServiceException("Cluster " + cloudCluster.getClusterName() + " already exists");
        }
        CreateClusterResult eksCluster = eksClient.createCluster(
                new CreateClusterRequest().withName(cloudCluster.getClusterName()).withRoleArn(cloudCluster.getRoleArn())
                        .withResourcesVpcConfig(new VpcConfigRequest().withSubnetIds(cloudCluster.getSubnetIds()).withSecurityGroupIds(cloudCluster.getSecurityGroupId())).withUpgradePolicy(new UpgradePolicyRequest().withSupportType(SupportType.STANDARD))
                        .withAccessConfig(new CreateAccessConfigRequest().withBootstrapClusterCreatorAdminPermissions(true)).withAccessConfig(new CreateAccessConfigRequest().withAuthenticationMode(AuthenticationMode.API_AND_CONFIG_MAP))
        );
        new Thread(new Runnable() {
            @Override
            public void run() {
                createClusterAddOns(cloudCluster, eksCluster.getCluster(), addOnList);
            }
        }).start();
        return eksCluster.getCluster();
    }


    public void createClusterAddOns(CloudCluster cloudCluster, Cluster cluster, List<String> addonNameList) {
        for(int i=0;i<20;i++) {
            if(Objects.equals(getCluster(cloudCluster).getStatus(), ClusterStatus.ACTIVE.toString())){
                break;
            }else{
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {

                }
            }
        }
        for (String addonName : addonNameList) {
            createClusterAddOn(cloudCluster, addonName);
        }
    }


    public Addon createClusterAddOn(CloudCluster cloudCluster, String addonName) {
        AmazonEKS eksClient = getClient(cloudCluster);
        String latestVersion = getAddonLatestVersion(eksClient, addonName);
        return eksClient.createAddon(new CreateAddonRequest().withClusterName(cloudCluster.getClusterName()).withAddonName(addonName)
                        .withServiceAccountRoleArn(cloudCluster.getRoleArn()).withAddonVersion(latestVersion))
                .getAddon();
    }

    private String getAddonLatestVersion(AmazonEKS eksClient, String addonName) {
        String latestVersion = "";
        String latestVersionPrefix = "";
        List<AddonVersionInfo> versionInfos = eksClient.describeAddonVersions(new DescribeAddonVersionsRequest().withAddonName(addonName)).getAddons().get(0).getAddonVersions();
        for (AddonVersionInfo versionInfo : versionInfos) {
            String version = versionInfo.getAddonVersion().replaceAll("v", "").split("-")[0].trim();
            if(latestVersionPrefix.isEmpty()){
                latestVersionPrefix = version;
                latestVersion = versionInfo.getAddonVersion();
            }else{
                if(new ComparableVersion(latestVersionPrefix).compareTo(new ComparableVersion(version)) < 0){
                    latestVersionPrefix = version;
                    latestVersion = versionInfo.getAddonVersion();
                }else if(new ComparableVersion(latestVersionPrefix).compareTo(new ComparableVersion(version)) == 0){
                    if(latestVersion.compareTo(versionInfo.getAddonVersion()) < 0){
                        latestVersion = versionInfo.getAddonVersion();
                    }

                }
            }
        }
        return latestVersion;
    }


    public Nodegroup createNodeGroup(CloudNodeGroup cloudNodeGroup) {

        CloudCluster cloudCluster = cloudClusterRepository.findByUid(cloudNodeGroup.getClusterUid());
        AmazonEKS eksClient = getClient(cloudCluster);

        //if (eksClient.listNodegroups(new ListNodegroupsRequest()).getNodegroups().contains(cloudNodeGroup.getGroupName())) {
            //throw new ResourceNotFoundException("Node group " + cloudNodeGroup.getGroupName() + " already exists!");
        //}


        CreateNodegroupResult eksCluster = eksClient.createNodegroup(
                new CreateNodegroupRequest().withClusterName(cloudCluster.getClusterName()).withNodegroupName(cloudNodeGroup.getGroupName()).withAmiType(cloudNodeGroup.getAmi())
                        .withInstanceTypes(cloudNodeGroup.getInstanceType()).withScalingConfig(new NodegroupScalingConfig()
                                .withDesiredSize(cloudNodeGroup.getDesiredNodeCount())
                                .withMinSize(cloudNodeGroup.getMinNodeCount())
                                .withMaxSize(cloudNodeGroup.getMaxNodeCount())).withNodeRole(cloudNodeGroup.getRoleArn()).withCapacityType(CapacityTypes.ON_DEMAND)
                        .withDiskSize(20).withSubnets(cloudNodeGroup.getSubnetIds()).withUpdateConfig(new NodegroupUpdateConfig().withMaxUnavailable(1))

        );
        return eksCluster.getNodegroup();
    }


    public Nodegroup updateScalingConfig(CloudNodeGroup cloudNodeGroup) {
        Nodegroup nodegroup = getNodeGroup(cloudNodeGroup);
        nodegroup.getScalingConfig()
                .withDesiredSize(cloudNodeGroup.getDesiredNodeCount())
                .withMinSize(cloudNodeGroup.getMinNodeCount())
                .withMaxSize(cloudNodeGroup.getMaxNodeCount());
        return nodegroup;

    }


    public Cluster getCluster(CloudCluster cloudCluster) {
        AmazonEKS eksClient = getClient(cloudCluster);
        if (eksClient.listClusters(new ListClustersRequest()).getClusters().contains(cloudCluster.getClusterName())) {
            return eksClient.describeCluster(new DescribeClusterRequest().withName(cloudCluster.getClusterName())).getCluster();
        }
        throw new ResourceNotFoundException("Cluster " + cloudCluster.getClusterName() + " not found");
    }


    public Nodegroup getNodeGroup(CloudNodeGroup cloudNodeGroup) {
        CloudCluster cloudCluster = cloudClusterRepository.findByUid(cloudNodeGroup.getClusterUid());
        AmazonEKS eksClient = getClient(cloudCluster);
        if (eksClient.listNodegroups(new ListNodegroupsRequest()).getNodegroups().contains(cloudNodeGroup.getGroupName())) {
            return eksClient.describeNodegroup(new DescribeNodegroupRequest().withNodegroupName(cloudNodeGroup.getGroupName())).getNodegroup();
        }

        throw new ResourceNotFoundException("Node group " + cloudNodeGroup.getGroupName() + " not found");
    }


    private AmazonEKS getClient(CloudCluster cloudCluster) {
        CloudCredential cloudCredential = cloudCredentialRepository.findByUid(cloudCluster.getCloudCredentialUid());
        AWSCredentials credentials = new BasicAWSCredentials(cloudCredential.getAccessKey(), cloudCredential.getSecretKey());

        return AmazonEKSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(cloudCluster.getRegion())
                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTPS).withMaxErrorRetry(DEFAULT_MAX_ERROR_RETRY).withRetryPolicy(new RetryPolicy(PredefinedRetryPolicies.DEFAULT_RETRY_CONDITION,
                        DEFAULT_BACKOFF_STRATEGY, DEFAULT_MAX_ERROR_RETRY, false))).build();
    }
}
