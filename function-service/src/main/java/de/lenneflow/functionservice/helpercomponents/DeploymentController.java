package de.lenneflow.functionservice.helpercomponents;

import de.lenneflow.functionservice.enums.DeploymentState;
import de.lenneflow.functionservice.exception.InternalServiceException;
import de.lenneflow.functionservice.feignclients.WorkerServiceClient;
import de.lenneflow.functionservice.feignmodels.AccessToken;
import de.lenneflow.functionservice.feignmodels.KubernetesCluster;
import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.repository.FunctionRepository;
import de.lenneflow.functionservice.util.YamlEditor;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.autoscaling.v2.*;
import io.fabric8.kubernetes.api.model.flowcontrol.v1.PolicyRulesWithSubjectsBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.PolicyRule;
import io.fabric8.kubernetes.api.model.rbac.PolicyRuleBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Helper class containing methods used for the deployment of a function.
 * @author Idrissa Ganemtore
 */
@Component
public class DeploymentController {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentController.class);

    public static final String NAMESPACE = "lenneflow";
    public static final String SERVICE_ACCOUNT_NAME = "lenneflow-sa";

    private final Random random = new Random();

    final WorkerServiceClient workerServiceClient;
    final FunctionRepository functionRepository;

    public DeploymentController(WorkerServiceClient workerServiceClient, FunctionRepository functionRepository) {
        this.workerServiceClient = workerServiceClient;
        this.functionRepository = functionRepository;
    }

    /**
     * Checks if the kubernetes cluster is reachable
     * @param kubernetesCluster the cluster to reach.
     */
    public void checkConnectionToKubernetes(KubernetesCluster kubernetesCluster) {
        KubernetesClient client = getKubernetesClient(kubernetesCluster);
        String apiVersion = client.getApiVersion();
        client.close();
        if(apiVersion == null || apiVersion.isEmpty()){
            logger.error("Impossible to connect to Kubernetes cluster");
            throw new InternalServiceException("The connection to the kubernetesCluster " + kubernetesCluster.getClusterName() + " was not possible");
        }
    }

    /**
     * Deploys a function to a randomly selected kubernetes cluster.
     * @param function the function to deploy
     */
    public void deployFunctionImageToWorker(Function function) {
        KubernetesCluster kubernetesCluster = getKubernetesClusterForFunction(function);
        checkConnectionToKubernetes(kubernetesCluster);
        assignHostPortToFunction(kubernetesCluster, function);
        KubernetesClient client = getKubernetesClient(kubernetesCluster);
        createNamespace(kubernetesCluster);
        createServiceAccount(kubernetesCluster);

        Deployment deployment = YamlEditor.createKubernetesDeploymentResource(function,1, SERVICE_ACCOUNT_NAME);
        Service service = YamlEditor.createKubernetesServiceResource(function, kubernetesCluster.getCloudProvider());
        client.resource(deployment).inNamespace(NAMESPACE).create();
        client.resource(service).inNamespace(NAMESPACE).create();
        createOrUpdateIngress(kubernetesCluster,function);
        String functionServiceUrl = getFunctionServiceUrl(kubernetesCluster, function);
        function.setServiceUrl(functionServiceUrl);
        functionRepository.save(function);
        new Thread(() -> waitAndUpdateDeploymentState(kubernetesCluster, function)).start();
    }

    public void undeployFunction(Function function) {
        KubernetesCluster kubernetesCluster = getKubernetesClusterForFunction(function);
        checkConnectionToKubernetes(kubernetesCluster);
        KubernetesClient client = getKubernetesClient(kubernetesCluster);
        client.services().inNamespace(NAMESPACE).withName(function.getName()).delete();
        client.apps().deployments().inNamespace(NAMESPACE).withName(function.getName()).delete();
        client.autoscaling().v1().horizontalPodAutoscalers().inNamespace(NAMESPACE).withName(function.getName()).delete();
        removeIngressPath(kubernetesCluster, function);

    }


    /**
     * This is the port that will be exposed to the host. This function assigns a port that is not in use
     * @param kubernetesCluster the kubernetes cluster
     * @param function the function to deploy
     */
    public void assignHostPortToFunction(KubernetesCluster kubernetesCluster, Function function){
        if(function.getAssignedHostPort() >= 47000){
            return;
        }
        List<Integer> ports = kubernetesCluster.getUsedHostPorts();
        if(ports.isEmpty()){
            ports.add(47000);
        }
        int nextPort = Collections.max(ports) + 1;
        function.setAssignedHostPort(nextPort);
        functionRepository.save(function);
        ports.add(nextPort);

        workerServiceClient.updateUsedPorts(kubernetesCluster.getUid(), ports);
    }

    /**
     * returns the full url of a deployed function.
     * @param kubernetesCluster the cluster
     * @param function the deployed function
     * @return the URL of the deployed function
     */
    private String getFunctionServiceUrl(KubernetesCluster kubernetesCluster, Function function) {
        String functionResourcePath = function.getResourcePath().startsWith("/") ? function.getResourcePath() : "/%s".formatted(function.getResourcePath());
        String hostUrl =  kubernetesCluster.getHostUrl();
        String hostUrl2 = hostUrl.replace("http://", "").replace("https://", "");
        return "http://" + hostUrl2 + functionResourcePath;
    }

    /**
     * After a kubernetes deployment of a function is done, it can take some minutes until it is ready.
     * This function tracks the state of the deployed resource in the background and updates the entity
     * @param kubernetesCluster the cluster
     * @param function the deployed function
     */
    private void waitAndUpdateDeploymentState(KubernetesCluster kubernetesCluster, Function function) {
            updateFunctionDeploymentState(function, DeploymentState.DEPLOYING);
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                KubernetesClient client = getKubernetesClient(kubernetesCluster);
                String deploymentName = function.getName();

                client.apps().deployments().inNamespace(NAMESPACE).withName(deploymentName).waitUntilCondition(
                        d -> (d.getStatus().getReadyReplicas() != null && d.getStatus().getReadyReplicas() > 0), 5, MINUTES);
                if(client.apps().deployments().inNamespace(NAMESPACE).withName(deploymentName).get().getStatus().getReadyReplicas() > 0){
                    updateFunctionDeploymentState(function, DeploymentState.DEPLOYED);
                    client.resource(createV2HorizontalPodsAutoscaler(deploymentName, 1, 10)).inNamespace(NAMESPACE).create();
                }
            }catch (Exception e){
                logger.error(e.getMessage());
                updateFunctionDeploymentState(function, DeploymentState.FAILED);
            }
    }

//    private HorizontalPodAutoscaler createV1HorizontalPodsAutoscaler(String deploymentName, int minPods, int maxPods) {
//        return new HorizontalPodAutoscalerBuilder()
//                .withNewMetadata()
//                .withName(deploymentName)
//                .addToLabels("name", deploymentName)
//                .endMetadata()
//                .withNewSpec()
//                .withNewScaleTargetRef()
//                .withApiVersion("apps/v1")
//                .withKind("Deployment")
//                .withName(deploymentName)
//                .endScaleTargetRef()
//                .withMinReplicas(minPods)
//                .withMaxReplicas(maxPods)
//                .withTargetCPUUtilizationPercentage(50)
//                .endSpec().build();
//    }

    private HorizontalPodAutoscaler createV2HorizontalPodsAutoscaler(String deploymentName, int minPods, int maxPods) {
        return new HorizontalPodAutoscalerBuilder()
                .withApiVersion("autoscaling/v2")
                .withNewMetadata()
                .withName(deploymentName)
                .addToLabels("name", deploymentName)
                .endMetadata()
                .withNewSpec()
                .withNewBehavior()
                .withNewScaleDown()
                .withStabilizationWindowSeconds(300)
                .withPolicies(new HPAScalingPolicyBuilder().withType("Pods").withValue(1).withPeriodSeconds(60).build())
                .endScaleDown()
                .withNewScaleUp()
                .withStabilizationWindowSeconds(60)
                .withPolicies(new HPAScalingPolicyBuilder().withType("Pods").withValue(1).withPeriodSeconds(60).build())
                .endScaleUp()
                .endBehavior()
                .withMinReplicas(minPods)
                .withMaxReplicas(maxPods)
                .withMetrics(new MetricSpecBuilder().withType("Resource").withNewResource().withName("cpu").withTarget(new MetricTargetBuilder().withType("Utilization").withAverageUtilization(50).build()).endResource().build())
                .endSpec().build();
    }

    /**
     * Updates the state of function entity
     * @param function function
     * @param deploymentState new state
     */
    private void updateFunctionDeploymentState(Function function, DeploymentState deploymentState) {
        function.setDeploymentState(deploymentState);
        functionRepository.save(function);
    }

    /**
     * Creates the ingress resource in the cluster. If a ingress resource already exists, it will update it.
     * @param kubernetesCluster the cluster
     * @param function the function that should be targeted in the ingress resource
     */
    private void createOrUpdateIngress(KubernetesCluster kubernetesCluster, Function function) {
        KubernetesClient client = getKubernetesClient(kubernetesCluster);
        Ingress currentIngress = client.network().v1().ingresses().inNamespace(NAMESPACE).withName(kubernetesCluster.getIngressServiceName()).get();
        assignHostPortToFunction(kubernetesCluster, function);
        try {
            if (currentIngress == null) {
                Ingress ingressResource = YamlEditor.createKubernetesIngressResource(kubernetesCluster, function);
                client.resource(ingressResource).inNamespace(NAMESPACE).create();
                return;
            }
            Ingress updatedIngressResource = YamlEditor.addPathToKubernetesIngressResource(currentIngress, function);
            client.resource(updatedIngressResource).inNamespace(NAMESPACE).patch();
        } catch (Exception e) {
            throw new InternalServiceException("It was not possible to create the ingress service ");
        }

    }

    private void removeIngressPath(KubernetesCluster kubernetesCluster, Function function) {
        KubernetesClient client = getKubernetesClient(kubernetesCluster);
        Ingress currentIngress = client.network().v1().ingresses().inNamespace(NAMESPACE).withName(kubernetesCluster.getIngressServiceName()).get();
        try {
            if (currentIngress != null) {
                if(currentIngress.getSpec().getRules().get(0).getHttp().getPaths().size() > 1){
                    currentIngress.getSpec().getRules().get(0).getHttp().getPaths().removeIf(path -> path.getPath().equals(function.getResourcePath()));
                    client.resource(currentIngress).inNamespace(NAMESPACE).patch();
                }else{
                    client.network().v1().ingresses().inNamespace(NAMESPACE).withName(kubernetesCluster.getIngressServiceName()).delete();
                }
            }
        } catch (Exception e) {
            throw new InternalServiceException("It was not possible to create the ingress service ");
        }
    }



    /**
     * Function that creates the default namespace for the application in the kubernetes cluster.
     * @param kubernetesCluster the target cluster
     */
    private void createNamespace(KubernetesCluster kubernetesCluster) {
        KubernetesClient client = getKubernetesClient(kubernetesCluster);
        Namespace ns = new NamespaceBuilder().withNewMetadata().withName(NAMESPACE)
                .endMetadata().build();
        try {
            client.namespaces().resource(ns).create();
        } catch (Exception e) {
            if(e.getMessage().toLowerCase().contains("already exists"))
                return;
            throw new InternalServiceException("It was not possible to create the namespace " + NAMESPACE + "\n" + e.getMessage());
        }
    }

    /**
     * Function that creates the default service account for the application in the kubernetes cluster.
     * @param kubernetesCluster the target cluster
     */
    private void createServiceAccount(KubernetesCluster kubernetesCluster) {
        KubernetesClient client = getKubernetesClient(kubernetesCluster);
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
            if(e.getMessage().toLowerCase().contains("already exists"))
                return;
            throw new InternalServiceException("It was not possible to create the service account " + SERVICE_ACCOUNT_NAME + "\n" + e.getMessage());
        }
    }

    /**
     * Randomly selects a cluster from a list a clusters tagged for this function type.
     * In case no cluster is tagged with the function type, a cluster will be randomly selected between all
     * existing clusters
     * @param function the function
     * @return the cluster
     */
    private KubernetesCluster getKubernetesClusterForFunction(Function function) {
        String functionType = function.getType();
        List<KubernetesCluster> filteredClusters = getClusters(functionType);

        //If no cluster for the function type exists, choose a random one.
        if (filteredClusters.isEmpty()) {
            List<KubernetesCluster> allClusters = workerServiceClient.getKubernetesClusterList();
            if(allClusters.isEmpty()) {
                throw new InternalServiceException("No worker found!");
            }
            return allClusters.get(random.nextInt(allClusters.size()));
        }

        //Choose a random cluster
        return filteredClusters.get(random.nextInt(filteredClusters.size()));
    }

    /**
     * Find the list of clusters that accepts the entered function type.
     * If no cluster for exists for the function type, an empty list will be returned.
     * @param functionType the function type
     * @return a list of clusters
     */
    private List<KubernetesCluster> getClusters(String functionType) {
        List<KubernetesCluster> kubernetesClusters = new ArrayList<>();
        List<KubernetesCluster> allClusters = workerServiceClient.getKubernetesClusterList();
        for (KubernetesCluster kubernetesCluster : allClusters) {
            if(kubernetesCluster.getSupportedFunctionTypes().contains(functionType)) {
                kubernetesClusters.add(kubernetesCluster);
            }
        }
        logger.debug("return all clusters");
        return kubernetesClusters;
    }

    /**
     * Creates a kubernetes client using the credentials.
     * @param kubernetesCluster the cluster
     * @return the kubernetes client Object
     */
    private KubernetesClient getKubernetesClient(KubernetesCluster kubernetesCluster) {
        AccessToken token = workerServiceClient.getK8sConnectionToken(kubernetesCluster.getUid());
        String  masterUrl = kubernetesCluster.getApiServerEndpoint();

        Config config = new ConfigBuilder()
                .withMasterUrl(masterUrl)
                .withTrustCerts(true)
                .withOauthToken(token.getToken())
                .build();
        return new KubernetesClientBuilder().withConfig(config).build();
    }
}
