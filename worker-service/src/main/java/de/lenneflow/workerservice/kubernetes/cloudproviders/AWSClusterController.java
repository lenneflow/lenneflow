package de.lenneflow.workerservice.kubernetes.cloudproviders;


import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.exception.PayloadNotValidException;
import de.lenneflow.workerservice.exception.ResourceNotFoundException;
import de.lenneflow.workerservice.kubernetes.IClusterController;
import de.lenneflow.workerservice.model.ClusterNodeGroup;
import de.lenneflow.workerservice.model.KubernetesCluster;
import de.lenneflow.workerservice.model.CloudCredential;
import de.lenneflow.workerservice.repository.KubernetesClusterRepository;
import de.lenneflow.workerservice.repository.CloudCredentialRepository;
import org.apache.commons.lang.time.DateUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4PresignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.*;
import software.amazon.awssdk.services.sts.endpoints.StsEndpointParams;
import software.amazon.awssdk.services.sts.endpoints.StsEndpointProvider;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;


@Component
public class AWSClusterController implements IClusterController {

    private final List<String> addOnList;
    private final CloudCredentialRepository cloudCredentialRepository;
    private final KubernetesClusterRepository kubernetesClusterRepository;

    public AWSClusterController(CloudCredentialRepository cloudCredentialRepository, KubernetesClusterRepository kubernetesClusterRepository) {
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

        Cluster cluster = eksClient.createCluster(CreateClusterRequest.builder().name(kubernetesCluster.getClusterName()).roleArn(kubernetesCluster.getRoleArn()).upgradePolicy(UpgradePolicyRequest.builder().supportType(SupportType.STANDARD).build())
                .resourcesVpcConfig(vpcConfigRequest).accessConfig(accessConfig1).accessConfig(accessConfig2).build()).cluster();

        new Thread(() -> createClusterAddOns(kubernetesCluster, addOnList)).start();
        return cluster;
    }


    public void createClusterAddOns(KubernetesCluster kubernetesCluster, List<String> addonNameList) {
        for(int i=0;i<20;i++) {
            if(Objects.equals(getCluster(kubernetesCluster).status().toString(), ClusterStatus.ACTIVE.toString())){
                kubernetesCluster.setStatus(de.lenneflow.workerservice.enums.ClusterStatus.CREATING_ADDONS);
                kubernetesClusterRepository.save(kubernetesCluster);
                break;
            }else{
                pause(60000);
            }
        }

        if(!Objects.equals(getCluster(kubernetesCluster).status().toString(), ClusterStatus.ACTIVE.toString())){
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
            if(!Objects.equals(addon.status().toString(), AddonStatus.ACTIVE.toString())){
                return false;
            }
        }
        return true;
    }


    public Addon createClusterAddOn(KubernetesCluster kubernetesCluster, String addonName) {
        EksClient eksClient = getClient(kubernetesCluster);
        String latestVersion = getAddonLatestVersion(eksClient, addonName);
        return eksClient.createAddon(CreateAddonRequest.builder().clusterName(kubernetesCluster.getClusterName()).addonName(addonName)
                        .serviceAccountRoleArn(kubernetesCluster.getRoleArn()).addonVersion(latestVersion).build())
                .addon();
    }

    private String getAddonLatestVersion(EksClient eksClient, String addonName) {
        String latestVersion = "";
        String latestVersionPrefix = "";
        List<AddonVersionInfo> versionInfos = eksClient.describeAddonVersions(DescribeAddonVersionsRequest.builder().addonName(addonName).build()).addons().get(0).addonVersions();
        for (AddonVersionInfo versionInfo : versionInfos) {
            String version = versionInfo.addonVersion().replace("v", "").split("-")[0].trim();
            if(latestVersionPrefix.isEmpty()){
                latestVersionPrefix = version;
                latestVersion = versionInfo.addonVersion();
            }else{
                if(new ComparableVersion(latestVersionPrefix).compareTo(new ComparableVersion(version)) < 0){
                    latestVersionPrefix = version;
                    latestVersion = versionInfo.addonVersion();
                }else if(new ComparableVersion(latestVersionPrefix).compareTo(new ComparableVersion(version)) == 0 && latestVersion.compareTo(versionInfo.addonVersion()) < 0){
                        latestVersion = versionInfo.addonVersion();
                }
            }
        }
        return latestVersion;
    }


    @Override
    public String getSessionToken(KubernetesCluster kubernetesCluster) {
        CloudCredential credential = cloudCredentialRepository.findByUid(kubernetesCluster.getCloudCredentialUid());
        AwsCredentials credentials = AwsBasicCredentials.create(credential.getAccessKey(), credential.getSecretKey());
        return getAuthenticationToken(credentials, Region.of(kubernetesCluster.getRegion()), kubernetesCluster.getClusterName());

    }

    public String getAuthenticationToken(AwsCredentials awsAuth, Region awsRegion, String clusterName) {
        SdkHttpFullRequest requestToSign = null;
        try {
            requestToSign = SdkHttpFullRequest
                    .builder()
                    .method(SdkHttpMethod.GET)
                    .uri(StsEndpointProvider.defaultProvider().resolveEndpoint(StsEndpointParams.builder().region(awsRegion).build()).get().url())
                    .appendHeader("x-k8s-aws-id", clusterName)
                    .appendRawQueryParameter("Action", "GetCallerIdentity")
                    .appendRawQueryParameter("Version", "2011-06-15")
                    .build();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        }
        Date expirationDate = DateUtils.addSeconds(Date.from(Instant.now()), 60);
        Aws4PresignerParams presignerParams = Aws4PresignerParams.builder()
                .awsCredentials(awsAuth)
                .signingRegion(awsRegion)
                .signingName("sts")
                .signingClockOverride(Clock.systemUTC())
                .expirationTime(expirationDate.toInstant())
                .build();

        SdkHttpFullRequest signedRequest = Aws4Signer.create().presign(requestToSign, presignerParams);

        String encodedUrl = Base64.getUrlEncoder().withoutPadding().encodeToString(signedRequest.getUri().toString().getBytes(StandardCharsets.UTF_8));
        return ("k8s-aws-v1." + encodedUrl);
    }


    public Nodegroup createNodeGroup(ClusterNodeGroup clusterNodeGroup) {

        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(clusterNodeGroup.getClusterUid());
        EksClient eksClient = getClient(kubernetesCluster);

        return eksClient.createNodegroup(
                CreateNodegroupRequest.builder().clusterName(kubernetesCluster.getClusterName()).nodegroupName(clusterNodeGroup.getGroupName()).amiType(clusterNodeGroup.getAmi())
                        .instanceTypes(clusterNodeGroup.getInstanceType()).scalingConfig(NodegroupScalingConfig.builder()
                                .desiredSize(clusterNodeGroup.getDesiredNodeCount())
                                .minSize(clusterNodeGroup.getMinNodeCount())
                                .maxSize(clusterNodeGroup.getMaxNodeCount()).build()).nodeRole(clusterNodeGroup.getRoleArn()).capacityType(CapacityTypes.ON_DEMAND)
                        .diskSize(20).subnets(clusterNodeGroup.getSubnetIds()).updateConfig(NodegroupUpdateConfig.builder().maxUnavailable(1).build())
                        .build()

        ).nodegroup();
    }

    public Nodegroup updateScalingConfig(ClusterNodeGroup clusterNodeGroup) {
        Nodegroup nodegroup = getNodeGroup(clusterNodeGroup);
        UpdateNodegroupConfigRequest.builder().nodegroupName(nodegroup.nodegroupName()).scalingConfig(NodegroupScalingConfig.builder()
                .desiredSize(clusterNodeGroup.getDesiredNodeCount())
                .minSize(clusterNodeGroup.getMinNodeCount())
                .maxSize(clusterNodeGroup.getMaxNodeCount()).build());
        return nodegroup;
    }

    public Cluster getCluster(KubernetesCluster kubernetesCluster) {
        EksClient eksClient = getClient(kubernetesCluster);
        if (eksClient.listClusters(ListClustersRequest.builder().build()).clusters().contains(kubernetesCluster.getClusterName())) {
            return eksClient.describeCluster(DescribeClusterRequest.builder().name(kubernetesCluster.getClusterName()).build()).cluster();
        }
        throw new ResourceNotFoundException("Cluster " + kubernetesCluster.getClusterName() + " not found");
    }


    public Nodegroup getNodeGroup(ClusterNodeGroup clusterNodeGroup) {
        KubernetesCluster kubernetesCluster = kubernetesClusterRepository.findByUid(clusterNodeGroup.getClusterUid());
        EksClient eksClient = getClient(kubernetesCluster);
        if (eksClient.listNodegroups(ListNodegroupsRequest.builder().build()).nodegroups().contains(clusterNodeGroup.getGroupName())) {
            return eksClient.describeNodegroup(DescribeNodegroupRequest.builder().nodegroupName(clusterNodeGroup.getGroupName()).build()).nodegroup();
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
            Thread.currentThread().interrupt();
        }
    }
}
