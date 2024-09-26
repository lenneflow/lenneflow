package de.lenneflow.functionservice.controller;

import de.lenneflow.functionservice.enums.DeploymentState;
import de.lenneflow.functionservice.exception.InternalServiceException;
import de.lenneflow.functionservice.feignclients.WorkerServiceClient;
import de.lenneflow.functionservice.feignmodels.ApiCredential;
import de.lenneflow.functionservice.feignmodels.KubernetesCluster;
import de.lenneflow.functionservice.model.Function;
import de.lenneflow.functionservice.repository.FunctionRepository;
import de.lenneflow.functionservice.util.YamlEditor;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceAccount;
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
public class KubernetesController {

    public static final String NAMESPACE = "lenneflow";
    public static final String SERVICE_ACCOUNT_NAME = "lenneflow-sa";

    private final Random random = new Random();

    final WorkerServiceClient workerServiceClient;
    final FunctionRepository functionRepository;

    public KubernetesController(WorkerServiceClient workerServiceClient, FunctionRepository functionRepository) {
        this.workerServiceClient = workerServiceClient;
        this.functionRepository = functionRepository;
    }

    public void checkWorkerConnection(KubernetesCluster kubernetesCluster) {
        KubernetesClient client = getKubernetesClient(kubernetesCluster);
        String apiVersion = client.getApiVersion();
        client.close();
        if(apiVersion == null || apiVersion.isEmpty()){
            throw new InternalServiceException("The connection to the kubernetesCluster " + kubernetesCluster.getClusterName() + " was not possible");
        }
    }

    public void checkServiceExists(KubernetesCluster kubernetesCluster) {
        KubernetesClient client = getKubernetesClient(kubernetesCluster);
        String apiVersion = client.getApiVersion();
        client.close();
        if(apiVersion == null || apiVersion.isEmpty()){
            throw new InternalServiceException("The connection to the kubernetesCluster " + kubernetesCluster.getClusterName() + " was not possible");
        }
    }

    public void deployFunctionImageToWorker(Function function) {
        KubernetesCluster kubernetesCluster = getKubernetesClusterForFunction(function);
        assignHostPortToFunction(kubernetesCluster, function);
        KubernetesClient client = getKubernetesClient(kubernetesCluster);
        createNamespace(kubernetesCluster);
        createServiceAccount(kubernetesCluster);
        Deployment deployment = YamlEditor.createKubernetesDeploymentResource(function,2, SERVICE_ACCOUNT_NAME);
        Service service = YamlEditor.createKubernetesServiceResource(function, "ClusterIP");
        client.resource(deployment).inNamespace(NAMESPACE).create();
        client.resource(service).inNamespace(NAMESPACE).create();
        createOrUpdateIngress(kubernetesCluster,function);
        String functionServiceUrl = "https://" + kubernetesCluster.getHostName() + function.getResourcePath();
        function.setServiceUrl(functionServiceUrl);
        functionRepository.save(function);
        updateDeploymentState(kubernetesCluster, function);
    }

    private void updateDeploymentState(KubernetesCluster kubernetesCluster, Function function) {
        new Thread(() ->{
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            updateFunction(function, DeploymentState.DEPLOYING);
            KubernetesClient client = getKubernetesClient(kubernetesCluster);
            String deploymentName = function.getName();

            client.apps().deployments().inNamespace(NAMESPACE).withName(deploymentName).waitUntilCondition(
                    d -> ( d.getStatus().getReadyReplicas() > 0), 5, MINUTES);
            if(client.apps().deployments().inNamespace(NAMESPACE).withName(deploymentName).isReady()){
                updateFunction(function, DeploymentState.DEPLOYED);
                return;
            }
            updateFunction(function, DeploymentState.FAILED);
        }).start();
    }

    private void updateFunction(Function function, DeploymentState deploymentState) {
        function.setDeploymentState(deploymentState);
        functionRepository.save(function);
    }

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
            if (client.namespaces().withName(NAMESPACE).get() != null) {
                return;
            }
            client.namespaces().resource(ns).create();
        } catch (Exception e) {
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
            throw new InternalServiceException("It was not possible to create the service account " + SERVICE_ACCOUNT_NAME + "\n" + e.getMessage());
        }
    }

    private KubernetesCluster getKubernetesClusterForFunction(Function function) {
        String functionType = function.getType();
        List<KubernetesCluster> kubernetesClusters = getClusters(functionType);
        if (kubernetesClusters.isEmpty()) {
            List<KubernetesCluster> workers2 = workerServiceClient.getKubernetesClusterList();
            if(workers2.isEmpty()) {
                throw new InternalServiceException("No worker found!");
            }
            return workers2.get(random.nextInt(workers2.size()));
        }
        return kubernetesClusters.get(random.nextInt(kubernetesClusters.size()));
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
        ApiCredential credential = workerServiceClient.getApiCredential(kubernetesCluster.getApiCredentialUid());
        String  masterUrl = credential.getApiServerEndpoint();

        Config config = new ConfigBuilder()
                .withMasterUrl(masterUrl)
                .withTrustCerts(true)
                .withOauthToken(credential.getApiAuthToken())
                .build();
        return new KubernetesClientBuilder().withConfig(config).build();
    }
}
