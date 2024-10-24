package de.lenneflow.functionservice.controller;

import de.lenneflow.functionservice.enums.CloudProvider;
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
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.concurrent.TimeUnit.MINUTES;

@Component
public class DeploymentController {

    public static final String NAMESPACE = "lenneflow";
    public static final String SERVICE_ACCOUNT_NAME = "lenneflow-sa";

    private final Random random = new Random();

    final WorkerServiceClient workerServiceClient;
    final FunctionRepository functionRepository;

    public DeploymentController(WorkerServiceClient workerServiceClient, FunctionRepository functionRepository) {
        this.workerServiceClient = workerServiceClient;
        this.functionRepository = functionRepository;
    }

    public void checkConnectionToKubernetes(KubernetesCluster kubernetesCluster) {
        KubernetesClient client = getKubernetesClient(kubernetesCluster);
        String apiVersion = client.getApiVersion();
        client.close();
        if(apiVersion == null || apiVersion.isEmpty()){
            throw new InternalServiceException("The connection to the kubernetesCluster " + kubernetesCluster.getClusterName() + " was not possible");
        }
    }

    public void deployFunctionImageToWorker(Function function) {
        KubernetesCluster kubernetesCluster = getKubernetesClusterForFunction(function);
        checkConnectionToKubernetes(kubernetesCluster);
        assignHostPortToFunction(kubernetesCluster, function);
        KubernetesClient client = getKubernetesClient(kubernetesCluster);
        createNamespace(kubernetesCluster);
        createServiceAccount(kubernetesCluster);

        Deployment deployment = YamlEditor.createKubernetesDeploymentResource(function,2, SERVICE_ACCOUNT_NAME);
        Service service = YamlEditor.createKubernetesServiceResource(function, kubernetesCluster.getCloudProvider());
        client.resource(deployment).inNamespace(NAMESPACE).create();
        client.resource(service).inNamespace(NAMESPACE).create();
        if(kubernetesCluster.getCloudProvider() == CloudProvider.LOCAL) {
            createOrUpdateIngress(kubernetesCluster,function);
        }
        String functionServiceUrl = getFunctionServiceUrl(kubernetesCluster, function);
        function.setServiceUrl(functionServiceUrl);
        functionRepository.save(function);
        waitAndUpdateDeploymentState(kubernetesCluster, function);
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

    private String getFunctionServiceUrl(KubernetesCluster kubernetesCluster, Function function) {
        String functionResourcePath = function.getResourcePath().startsWith("/") ? function.getResourcePath() : "/%s".formatted(function.getResourcePath());
        if(kubernetesCluster.getCloudProvider() == CloudProvider.LOCAL) {
            return kubernetesCluster.getHostAddress() + functionResourcePath;
        }
        return getLoadBalancerAssignedHostName(kubernetesCluster) + ":" + function.getAssignedHostPort() + functionResourcePath;
    }

    private String getLoadBalancerAssignedHostName(KubernetesCluster kubernetesCluster) {
        String hostname = "";
        //TODO
        return hostname;
    }

    private void waitAndUpdateDeploymentState(KubernetesCluster kubernetesCluster, Function function) {
        new Thread(() ->{
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            updateFunctionDeploymentState(function, DeploymentState.DEPLOYING);
            KubernetesClient client = getKubernetesClient(kubernetesCluster);
            String deploymentName = function.getName();

            client.apps().deployments().inNamespace(NAMESPACE).withName(deploymentName).waitUntilCondition(
                    d -> ( d.getStatus().getReadyReplicas() > 0), 5, MINUTES);
            if(client.apps().deployments().inNamespace(NAMESPACE).withName(deploymentName).isReady()){
                updateFunctionDeploymentState(function, DeploymentState.DEPLOYED);
                return;
            }
            updateFunctionDeploymentState(function, DeploymentState.FAILED);
        }).start();
    }

    private void updateFunctionDeploymentState(Function function, DeploymentState deploymentState) {
        function.setDeploymentState(deploymentState);
        functionRepository.save(function);
    }

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

    private List<KubernetesCluster> getClusters(String functionType) {
        List<KubernetesCluster> kubernetesClusters = new ArrayList<>();
        List<KubernetesCluster> allClusters = workerServiceClient.getKubernetesClusterList();
        for (KubernetesCluster kubernetesCluster : allClusters) {
            if(kubernetesCluster.getSupportedFunctionTypes().contains(functionType)) {
                kubernetesClusters.add(kubernetesCluster);
            }
        }
        return kubernetesClusters;
    }

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
