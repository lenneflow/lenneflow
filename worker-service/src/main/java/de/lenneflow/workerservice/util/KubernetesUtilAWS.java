package de.lenneflow.workerservice.util;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.feignclients.FunctionServiceClient;
import de.lenneflow.workerservice.feignmodel.Function;
import de.lenneflow.workerservice.repository.WorkerRepository;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static com.amazonaws.retry.PredefinedRetryPolicies.DEFAULT_BACKOFF_STRATEGY;

@Component
public class KubernetesUtilAWS {

    public static final String NAMESPACE = "lenneflow";
    public static final String SERVICE_ACCOUNT_NAME = "lenneflow-sa";
    public static final int DEFAULT_MAX_ERROR_RETRY = 2;

    final
    WorkerRepository workerRepository;
    final FunctionServiceClient functionServiceClient;

    public KubernetesUtilAWS(WorkerRepository workerRepository, FunctionServiceClient functionServiceClient) {
        this.workerRepository = workerRepository;
        this.functionServiceClient = functionServiceClient;
    }

    public static Cluster createOrgetCluster(String accessKey, String secretKey, String region, String clusterName, String roleArn) {
        AmazonEKS eksClient = getEksClient(accessKey, secretKey, region);
        String[] subnetIds = {"subnet-03c141faec217c66d", "subnet-0c5ebbab15fb869e4"};
        String securityGroupId = "sg-0909473246eeba42a";
        if (eksClient.listClusters(new ListClustersRequest()).getClusters().contains(clusterName)) {
            return eksClient.describeCluster(new DescribeClusterRequest().withName(clusterName)).getCluster();
        }
        CreateClusterResult eksCluster = eksClient.createCluster(
                new CreateClusterRequest().withName(clusterName).withRoleArn(roleArn)
                        .withResourcesVpcConfig(new VpcConfigRequest().withSubnetIds(subnetIds).withSecurityGroupIds(securityGroupId)).withUpgradePolicy(new UpgradePolicyRequest().withSupportType(SupportType.STANDARD))
                        .withAccessConfig(new CreateAccessConfigRequest().withBootstrapClusterCreatorAdminPermissions(true)).withAccessConfig(new CreateAccessConfigRequest().withAuthenticationMode(AuthenticationMode.API_AND_CONFIG_MAP))


        );
        return eksCluster.getCluster();
    }

    public Nodegroup createOrGetNodeGroup(String accessKey, String secretKey, String region, String amiType, String instanceType, int maxNodeCount, Cluster cluster) {

        AmazonEKS eksClient = getEksClient(accessKey, secretKey, region);
        String nodeGroupName = cluster.getName() + "-ng";

        if (eksClient.listNodegroups(new ListNodegroupsRequest()).getNodegroups().contains(nodeGroupName)) {
            return eksClient.describeNodegroup(new DescribeNodegroupRequest().withNodegroupName(nodeGroupName)).getNodegroup();
        }

        CreateNodegroupResult eksCluster = eksClient.createNodegroup(
                new CreateNodegroupRequest().withClusterName(cluster.getName()).withNodegroupName(nodeGroupName).withAmiType(amiType)
                        .withInstanceTypes(instanceType).withScalingConfig(new NodegroupScalingConfig().withDesiredSize(1).withMinSize(1).withMaxSize(maxNodeCount))
        );
        return eksCluster.getNodegroup();
    }


    private static AmazonEKS getEksClient(String accessKey, String secretKey, String region) {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTPS);
        clientConfig.setMaxErrorRetry(DEFAULT_MAX_ERROR_RETRY);
        clientConfig.setRetryPolicy(new RetryPolicy(PredefinedRetryPolicies.DEFAULT_RETRY_CONDITION,
                DEFAULT_BACKOFF_STRATEGY, DEFAULT_MAX_ERROR_RETRY, false));

        return AmazonEKSClientBuilder.standard()
                .withClientConfiguration(clientConfig)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    private static KubernetesClient getKubernetesClient() {
        Config config = new ConfigBuilder()
                .withMasterUrl("https://CB9C548E260E71144A8B2F7A29990364.gr7.us-east-2.eks.amazonaws.com")
                .withTrustCerts(true)
                //.withUsername("idrisseks")
                .withAutoOAuthToken("k8s-aws-v1.aHR0cHM6Ly9zdHMuYW1hem9uYXdzLmNvbS8_QWN0aW9uPUdldENhbGxlcklkZW50aXR5JlZlcnNpb249MjAxMS0wNi0xNSZYLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUE0TVRXT0FSVFFGQlVBNE1YJTJGMjAyNDA5MDclMkZ1cy1lYXN0LTElMkZzdHMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI0MDkwN1QxNjQ5MTVaJlgtQW16LUV4cGlyZXM9NjAmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0JTNCeC1rOHMtYXdzLWlkJlgtQW16LVNpZ25hdHVyZT0zNDRhOTExOGRkNjdjYjczMGVkNTNjNGFmYzUxMjE0NDEwNzhmNjYyZDk5MzgzZjc4MjMzNDUxOTM0YjliYjk3")
                .build();
        return new KubernetesClientBuilder().withConfig(config).build();
    }

    public static void deployFunctionImageToWorker(Function function) {
        KubernetesClient client = getKubernetesClient();
        createNamespace();
        createServiceAccount();
        Deployment deployment = YamlEditor.createKubernetesDeploymentResource(function,2, SERVICE_ACCOUNT_NAME);
        Service service = YamlEditor.createKubernetesServiceResource(function, "ClusterIP");
        client.resource(deployment).inNamespace(NAMESPACE).create();
        client.resource(service).inNamespace(NAMESPACE).create();
    }

    private static void createNamespace() {
        KubernetesClient client = getKubernetesClient();
        Namespace ns = new NamespaceBuilder().withNewMetadata().withName(NAMESPACE)
                .endMetadata().build();
        try {
            if (client.namespaces().withName(NAMESPACE).get() != null) {
                return;
            }
            client.namespaces().resource(ns).create();
        } catch (Exception e) {
            throw new InternalServiceException("It was not possible to create the namespace " + NAMESPACE + "\n" + e.getMessage());
        }
    }

    private static void createServiceAccount() {
        KubernetesClient client = getKubernetesClient();
        try {
            if (client.serviceAccounts().inNamespace(NAMESPACE).withName(SERVICE_ACCOUNT_NAME).get() == null) {
                ServiceAccount serviceAccountResource = YamlEditor.createKubernetesServiceAccountResource(SERVICE_ACCOUNT_NAME);
                client.resource(serviceAccountResource).inNamespace(NAMESPACE).create();
                ClusterRole roleResource = YamlEditor.createKubernetesClusterRoleResource(SERVICE_ACCOUNT_NAME, NAMESPACE);
                client.resource(roleResource).inNamespace(NAMESPACE).create();
                ClusterRoleBinding bindingResource = YamlEditor.createKubernetesClusterRoleBindingResource(SERVICE_ACCOUNT_NAME, NAMESPACE);
                client.resource(bindingResource).inNamespace(NAMESPACE).create();
            }
        } catch (Exception e) {
            throw new InternalServiceException("It was not possible to create the service account " + SERVICE_ACCOUNT_NAME + "\n" + e.getMessage());
        }

    }

    public static void deployNginxController() {
        try {
            KubernetesClient client = getKubernetesClient();

            File file = ResourceUtils.getFile("classpath:nginxingresscontroller.yaml");

            Yaml yaml = new Yaml();
            ObjectMapper mapper = new YAMLMapper();

            Iterable<Object> obj = yaml.loadAll(new FileInputStream(file));
            for (Object o : obj) {
                String resource = mapper.writeValueAsString(o);
                client.resource(resource).create();
            }
        } catch (Exception e) {
            throw new InternalServiceException(e.getMessage());
        }
    }

    private static String getAddonLatestVersion(AmazonEKS eksClient, String addonName) {
        String latestVersion = "";
        String latestVersionPrefix = "";
        List<AddonVersionInfo> versionInfos = eksClient.describeAddonVersions(new DescribeAddonVersionsRequest().withAddonName(addonName)).getAddons().get(0).getAddonVersions();
        for (AddonVersionInfo versionInfo : versionInfos) {
            String version = versionInfo.getAddonVersion().replaceAll("v", "").split("-")[0].trim();
            System.out.println(version);
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

    public static void main(String[] args) {
        //createOrgetCluster("AKIA4MTWOARTSUH6GIL2", "MuvkpDiE9s3wR0zeeWpa1k/Q0Lr1qoG3W0OcrTCy", "us-east-2", "idrissCluster", "arn:aws:iam::851725648999:role/aws-service-role/eks.amazonaws.com/AWSServiceRoleForAmazonEKS");
        AmazonEKS eksClient = getEksClient("AKIA4MTWOARTQFBUA4MX","GOf6T7j5uIlhJq28ah1bvcYETqwCMZUagIw1Q2gi","us-east-2");
        //CloudCluster cluster = eksClient.describeCluster(new DescribeClusterRequest().withName("Cluster1")).getCluster();
        //System.out.println(cluster.getCertificateAuthority());

        Function function = new Function();
        function.setName("functionjava");
        function.setAssignedHostPort(47000);
        function.setServingPort(8080);
        function.setInputSchema("placeholder");
        function.setResourcePath("/api/functionjava/process");
        function.setImageName("lenneflow/function-java");

        //KubernetesClient client = getKubernetesClient();
        //client.services().list().getItems().forEach(System.out::println);

        System.out.println(getAddonLatestVersion(eksClient, "eks-pod-identity-agent"));
    }

}
