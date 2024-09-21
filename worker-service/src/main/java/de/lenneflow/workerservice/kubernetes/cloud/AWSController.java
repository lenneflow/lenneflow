package de.lenneflow.workerservice.kubernetes.cloud;

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
import de.lenneflow.workerservice.exception.PayloadNotValidException;
import de.lenneflow.workerservice.exception.ResourceNotFoundException;
import de.lenneflow.workerservice.model.KubernetesCluster;
import de.lenneflow.workerservice.model.CloudCredential;
import de.lenneflow.workerservice.model.CloudNodeGroup;
import de.lenneflow.workerservice.repository.KubernetesClusterRepository;
import de.lenneflow.workerservice.repository.CloudCredentialRepository;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.amazonaws.retry.PredefinedRetryPolicies.DEFAULT_BACKOFF_STRATEGY;
import static com.amazonaws.retry.PredefinedRetryPolicies.DEFAULT_MAX_ERROR_RETRY;

@Component
public class AWSController implements ICloudController{

    private final List<String> addOnList;
    private final CloudCredentialRepository cloudCredentialRepository;
    private final KubernetesClusterRepository kubernetesClusterRepository;

    public AWSController(CloudCredentialRepository cloudCredentialRepository, KubernetesClusterRepository kubernetesClusterRepository) {
        this.cloudCredentialRepository = cloudCredentialRepository;
        this.kubernetesClusterRepository = kubernetesClusterRepository;
        addOnList = Arrays.asList("kube-proxy", "vpc-cni", "eks-pod-identity-agent", "coredns");
    }

    public Cluster createCluster(KubernetesCluster kubernetesCluster) {
        AmazonEKS eksClient = getClient(kubernetesCluster);
        if (eksClient.listClusters(new ListClustersRequest()).getClusters().contains(kubernetesCluster.getClusterName())) {
            throw new PayloadNotValidException("Cluster " + kubernetesCluster.getClusterName() + " already exists in the cloud");
        }
        CreateClusterResult eksCluster = eksClient.createCluster(
                new CreateClusterRequest().withName(kubernetesCluster.getClusterName()).withRoleArn(kubernetesCluster.getRoleArn())
                        .withResourcesVpcConfig(new VpcConfigRequest().withSubnetIds(kubernetesCluster.getSubnetIds()).withSecurityGroupIds(kubernetesCluster.getSecurityGroupId())).withUpgradePolicy(new UpgradePolicyRequest().withSupportType(SupportType.STANDARD))
                        .withAccessConfig(new CreateAccessConfigRequest().withBootstrapClusterCreatorAdminPermissions(true)).withAccessConfig(new CreateAccessConfigRequest().withAuthenticationMode(AuthenticationMode.API_AND_CONFIG_MAP))
        );
        new Thread(() -> createClusterAddOns(kubernetesCluster, eksCluster.getCluster(), addOnList)).start();
        return eksCluster.getCluster();
    }


    public void createClusterAddOns(KubernetesCluster kubernetesCluster, Cluster cluster, List<String> addonNameList) {
        for(int i=0;i<20;i++) {
            if(Objects.equals(getCluster(kubernetesCluster).getStatus(), ClusterStatus.ACTIVE.toString())){
                kubernetesCluster.setStatus(de.lenneflow.workerservice.enums.ClusterStatus.CREATING_ADDONS);
                kubernetesClusterRepository.save(kubernetesCluster);
                break;
            }else{
                pause(60000);
            }
        }

        if(!Objects.equals(getCluster(kubernetesCluster).getStatus(), ClusterStatus.ACTIVE.toString())){
            kubernetesClusterRepository.delete(kubernetesCluster);
           throw new InternalServiceException("Cluster " + kubernetesCluster.getClusterName() + " could not be created!");
        }

        List<Addon> addons = new ArrayList<>();
        for (String addonName : addonNameList) {
            addons.add(createClusterAddOn(kubernetesCluster, addonName));
        }

        for(int i=0;i<20;i++) {
            if(allAddOnsActive(addons)){
                break;
            }else{
             pause(10000);
            }
        }

        kubernetesCluster.setStatus(de.lenneflow.workerservice.enums.ClusterStatus.CREATED);
        kubernetesClusterRepository.save(kubernetesCluster);
    }

    private boolean allAddOnsActive(List<Addon> addons){
        for(Addon addon : addons){
            if(!Objects.equals(addon.getStatus(), AddonStatus.ACTIVE.toString())){
                return false;
            }
        }
        return true;
    }


    public Addon createClusterAddOn(KubernetesCluster kubernetesCluster, String addonName) {
        AmazonEKS eksClient = getClient(kubernetesCluster);
        String latestVersion = getAddonLatestVersion(eksClient, addonName);
        return eksClient.createAddon(new CreateAddonRequest().withClusterName(kubernetesCluster.getClusterName()).withAddonName(addonName)
                        .withServiceAccountRoleArn(kubernetesCluster.getRoleArn()).withAddonVersion(latestVersion))
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

        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(cloudNodeGroup.getClusterUid());
        AmazonEKS eksClient = getClient(kubernetesCluster);

        //if (eksClient.listNodegroups(new ListNodegroupsRequest()).getNodegroups().contains(cloudNodeGroup.getGroupName())) {
            //throw new ResourceNotFoundException("Node group " + cloudNodeGroup.getGroupName() + " already exists!");
        //}


        CreateNodegroupResult eksCluster = eksClient.createNodegroup(
                new CreateNodegroupRequest().withClusterName(kubernetesCluster.getClusterName()).withNodegroupName(cloudNodeGroup.getGroupName()).withAmiType(cloudNodeGroup.getAmi())
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

    public Cluster getCluster(KubernetesCluster kubernetesCluster) {
        AmazonEKS eksClient = getClient(kubernetesCluster);
        if (eksClient.listClusters(new ListClustersRequest()).getClusters().contains(kubernetesCluster.getClusterName())) {
            return eksClient.describeCluster(new DescribeClusterRequest().withName(kubernetesCluster.getClusterName())).getCluster();
        }
        throw new ResourceNotFoundException("Cluster " + kubernetesCluster.getClusterName() + " not found");
    }

//    public Cluster getSessionToken(KubernetesCluster cloudCluster) {
//        AmazonEKS eksClient = getClient(cloudCluster);
//        eksClient.
//
//
//    }

    public Nodegroup getNodeGroup(CloudNodeGroup cloudNodeGroup) {
        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(cloudNodeGroup.getClusterUid());
        AmazonEKS eksClient = getClient(kubernetesCluster);
        if (eksClient.listNodegroups(new ListNodegroupsRequest()).getNodegroups().contains(cloudNodeGroup.getGroupName())) {
            return eksClient.describeNodegroup(new DescribeNodegroupRequest().withNodegroupName(cloudNodeGroup.getGroupName())).getNodegroup();
        }
        throw new ResourceNotFoundException("Node group " + cloudNodeGroup.getGroupName() + " not found");
    }

    private AmazonEKS getClient(KubernetesCluster kubernetesCluster) {
        CloudCredential cloudCredential = cloudCredentialRepository.findByUid(kubernetesCluster.getCloudCredentialUid());
        AWSCredentials credentials = new BasicAWSCredentials(cloudCredential.getAccessKey(), cloudCredential.getSecretKey());

        return AmazonEKSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(kubernetesCluster.getRegion())
                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTPS).withMaxErrorRetry(DEFAULT_MAX_ERROR_RETRY).withRetryPolicy(new RetryPolicy(PredefinedRetryPolicies.DEFAULT_RETRY_CONDITION,
                        DEFAULT_BACKOFF_STRATEGY, DEFAULT_MAX_ERROR_RETRY, false))).build();
    }

    private void pause(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
