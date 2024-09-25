package de.lenneflow.workerservice.kubernetes.cloud;


import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.exception.PayloadNotValidException;
import de.lenneflow.workerservice.exception.ResourceNotFoundException;
import de.lenneflow.workerservice.model.ClusterNodeGroup;
import de.lenneflow.workerservice.model.KubernetesCluster;
import de.lenneflow.workerservice.model.CloudCredential;
import de.lenneflow.workerservice.repository.KubernetesClusterRepository;
import de.lenneflow.workerservice.repository.CloudCredentialRepository;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.internal.http.loader.DefaultSdkHttpClientBuilder;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.*;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;



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
        EksClient eksClient = getClient(kubernetesCluster);
        if (eksClient.listClusters().clusters().contains(kubernetesCluster.getClusterName())) {
            throw new PayloadNotValidException("The Cluster " + kubernetesCluster.getClusterName() + " already exists in the cloud");
        }

        VpcConfigRequest vpcConfigRequest = VpcConfigRequest.builder().subnetIds(kubernetesCluster.getSubnetIds()).securityGroupIds(kubernetesCluster.getSecurityGroupId()).build();
        CreateAccessConfigRequest accessConfig1 = CreateAccessConfigRequest.builder().bootstrapClusterCreatorAdminPermissions(true).build();
        CreateAccessConfigRequest accessConfig2 = CreateAccessConfigRequest.builder().authenticationMode(AuthenticationMode.API_AND_CONFIG_MAP).build();

        eksClient.createCluster(CreateClusterRequest.builder().name(kubernetesCluster.getClusterName()).roleArn(kubernetesCluster.getRoleArn())
                .resourcesVpcConfig(vpcConfigRequest).accessConfig(accessConfig1).accessConfig(accessConfig2).build());

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

    public String getAuthenticationToken(AWSCredentialsProvider awsAuth, Region awsRegion, String clusterName) {
        try {
            SdkHttpFullRequest requestToSign = SdkHttpFullRequest
                    .builder()
                    .method(SdkHttpMethod.GET)
                    .uri(StsUtil.getStsRegionalEndpointUri(awsRegion))
                    .appendHeader("x-k8s-aws-id", clusterName)
                    .appendRawQueryParameter("Action", "GetCallerIdentity")
                    .appendRawQueryParameter("Version", "2011-06-15")
                    .build();

            ZonedDateTime expirationDate = DateUtil.addSeconds(DateUtil.now(), 60);
            Aws4PresignerParams presignerParams = Aws4PresignerParams.builder()
                    .awsCredentials(awsAuth.resolveCredentials())
                    .signingRegion(awsRegion)
                    .signingName("sts")
                    .signingClockOverride(Clock.systemUTC())
                    .expirationTime(expirationDate.toInstant())
                    .build();

            SdkHttpFullRequest signedRequest = Aws4Signer.create().presign(requestToSign, presignerParams);

            String encodedUrl = Base64.getUrlEncoder().withoutPadding().encodeToString(signedRequest.getUri().toString().getBytes(CharSet.UTF_8.getCharset()));
            return ("k8s-aws-v1." + encodedUrl);
        } catch (Exception e) {
            String errorMessage = "A problem occurred generating an Eks authentication token for cluster: " + clusterName;
            //logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }


    public Nodegroup createNodeGroup(ClusterNodeGroup clusterNodeGroup) {

        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(clusterNodeGroup.getClusterUid());
        AmazonEKS eksClient = getClient(kubernetesCluster);

        //if (eksClient.listNodegroups(new ListNodegroupsRequest()).getNodegroups().contains(clusterNodeGroup.getGroupName())) {
            //throw new ResourceNotFoundException("Node group " + clusterNodeGroup.getGroupName() + " already exists!");
        //}


        CreateNodegroupResult eksCluster = eksClient.createNodegroup(
                new CreateNodegroupRequest().withClusterName(kubernetesCluster.getClusterName()).withNodegroupName(clusterNodeGroup.getGroupName()).withAmiType(clusterNodeGroup.getAmi())
                        .withInstanceTypes(clusterNodeGroup.getInstanceType()).withScalingConfig(new NodegroupScalingConfig()
                                .withDesiredSize(clusterNodeGroup.getDesiredNodeCount())
                                .withMinSize(clusterNodeGroup.getMinNodeCount())
                                .withMaxSize(clusterNodeGroup.getMaxNodeCount())).withNodeRole(clusterNodeGroup.getRoleArn()).withCapacityType(CapacityTypes.ON_DEMAND)
                        .withDiskSize(20).withSubnets(clusterNodeGroup.getSubnetIds()).withUpdateConfig(new NodegroupUpdateConfig().withMaxUnavailable(1))

        );
        return eksCluster.getNodegroup();
    }

    public Nodegroup updateScalingConfig(ClusterNodeGroup clusterNodeGroup) {
        Nodegroup nodegroup = getNodeGroup(clusterNodeGroup);
        nodegroup.getScalingConfig()
                .withDesiredSize(clusterNodeGroup.getDesiredNodeCount())
                .withMinSize(clusterNodeGroup.getMinNodeCount())
                .withMaxSize(clusterNodeGroup.getMaxNodeCount());
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

    public Nodegroup getNodeGroup(ClusterNodeGroup clusterNodeGroup) {
        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(clusterNodeGroup.getClusterUid());
        AmazonEKS eksClient = getClient(kubernetesCluster);
        if (eksClient.listNodegroups(new ListNodegroupsRequest()).getNodegroups().contains(clusterNodeGroup.getGroupName())) {
            return eksClient.describeNodegroup(new DescribeNodegroupRequest().withNodegroupName(clusterNodeGroup.getGroupName())).getNodegroup();
        }
        throw new ResourceNotFoundException("Node group " + clusterNodeGroup.getGroupName() + " not found");
    }

    private EksClient getClient(KubernetesCluster kubernetesCluster) {
        CloudCredential cloudCredential = cloudCredentialRepository.findByUid(kubernetesCluster.getCloudCredentialUid());
        AwsCredentials credentials = AwsBasicCredentials.create(cloudCredential.getAccessKey(), cloudCredential.getSecretKey());

        return EksClient.builder().region(Region.of(kubernetesCluster.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private void pause(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
