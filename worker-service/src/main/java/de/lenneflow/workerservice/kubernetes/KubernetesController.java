package de.lenneflow.workerservice.kubernetes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.lenneflow.workerservice.enums.DeploymentState;
import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.feignclients.FunctionServiceClient;
import de.lenneflow.workerservice.feignmodel.Function;
import de.lenneflow.workerservice.model.LocalCluster;
import de.lenneflow.workerservice.repository.WorkerRepository;
import de.lenneflow.workerservice.util.YamlEditor;
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
import org.yaml.snakeyaml.Yaml;

import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.util.concurrent.TimeUnit.MINUTES;

@Component
public class KubernetesController {

    public static final String NAMESPACE = "lenneflow";
    public static final String SERVICE_ACCOUNT_NAME = "lenneflow-sa";

    final
    WorkerRepository workerRepository;
    final FunctionServiceClient functionServiceClient;

    public KubernetesController(WorkerRepository workerRepository, FunctionServiceClient functionServiceClient) {
        this.workerRepository = workerRepository;
        this.functionServiceClient = functionServiceClient;
    }

    public void checkWorkerConnection(LocalCluster localCluster) {
        KubernetesClient client = getKubernetesClient(localCluster);
        String apiVersion = client.getApiVersion();
        client.close();
        if(apiVersion == null || apiVersion.isEmpty()){
            throw new InternalServiceException("The connection to the localCluster " + localCluster.getName() + " was not possible");
        }
    }

    public void checkServiceExists(LocalCluster localCluster) {
        KubernetesClient client = getKubernetesClient(localCluster);
        String apiVersion = client.getApiVersion();
        client.close();
        if(apiVersion == null || apiVersion.isEmpty()){
            throw new InternalServiceException("The connection to the localCluster " + localCluster.getName() + " was not possible");
        }
    }

    public void deployFunctionImageToWorker(Function function) {
        LocalCluster localCluster = getWorkerForFunction(function);
        assignHostPortToFunction(localCluster, function);
        KubernetesClient client = getKubernetesClient(localCluster);
        createNamespace(localCluster);
        createServiceAccount(localCluster);
        Deployment deployment = YamlEditor.createKubernetesDeploymentResource(function,2, SERVICE_ACCOUNT_NAME);
        Service service = YamlEditor.createKubernetesServiceResource(function, "ClusterIP");
        client.resource(deployment).inNamespace(NAMESPACE).create();
        client.resource(service).inNamespace(NAMESPACE).create();
        createOrUpdateIngress(localCluster,function);
        String functionServiceUrl = "https://" + localCluster.getHostName() + function.getResourcePath();
        function.setServiceUrl(functionServiceUrl);
        functionServiceClient.updateFunction(function, function.getUid());
        updateDeploymentState(localCluster, function);
    }

    private void updateDeploymentState(LocalCluster localCluster, Function function) {
        new Thread(() ->{
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            updateFunction(function, DeploymentState.DEPLOYING);
            KubernetesClient client = getKubernetesClient(localCluster);
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
        functionServiceClient.updateFunction(function, function.getUid());
    }

    public void assignHostPortToFunction(LocalCluster localCluster, Function function){
        if(function.getAssignedHostPort() >= 47000){
            return;
        }
        List<Integer> ports = localCluster.getUsedHostPorts();
        if(ports.isEmpty()){
            ports.add(47000);
        }
        int nextPort = Collections.max(ports) + 1;
        function.setAssignedHostPort(nextPort);
        functionServiceClient.updateFunction(function, function.getUid());
        ports.add(nextPort);
        localCluster.setUsedHostPorts(ports);
        workerRepository.save(localCluster);
    }

    public void deployFunctionToWorker(Function function, List<String> deploymentFileUrls) {
        try {
            LocalCluster localCluster = getWorkerForFunction(function);
            KubernetesClient client = getKubernetesClient(localCluster);
            createNamespace(localCluster);

            Yaml yaml = new Yaml();
            ObjectMapper mapper = new YAMLMapper();
            for (String deploymentFileUrl : deploymentFileUrls) {
                URL url = new URL(deploymentFileUrl);
                URLConnection connection = url.openConnection();

                Map<String, Object> obj = yaml.load(connection.getInputStream());
                String resource = mapper.writeValueAsString(obj);
                client.resource(resource).inNamespace(NAMESPACE).create();
            }
        } catch (Exception e) {
            throw new InternalServiceException(e.getMessage());
        }
    }

    private void createOrUpdateIngress(LocalCluster localCluster, Function function) {
        KubernetesClient client = getKubernetesClient(localCluster);
        Ingress currentIngress = client.network().v1().ingresses().inNamespace(NAMESPACE).withName(localCluster.getIngressServiceName()).get();
        assignHostPortToFunction(localCluster, function);
        try {
            if (currentIngress == null) {
                Ingress ingressResource = YamlEditor.createKubernetesIngressResource(localCluster, function);
                currentIngress = client.resource(ingressResource).inNamespace(NAMESPACE).create();
                return;
            }
            assert currentIngress != null;
            Ingress updatedIngressResource = YamlEditor.addPathToKubernetesIngressResource(currentIngress, function);
            client.resource(updatedIngressResource).inNamespace(NAMESPACE).patch();
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServiceException("It was not possible to create the ingress service ");
        }

    }

    private void createNamespace(LocalCluster localCluster) {
        KubernetesClient client = getKubernetesClient(localCluster);
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

    private void createServiceAccount(LocalCluster localCluster) {
        KubernetesClient client = getKubernetesClient(localCluster);
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

    private LocalCluster getWorkerForFunction(Function function) {
        Random random = new Random();
        String functionType = function.getType();
        List<LocalCluster> localClusters = workerRepository.findBySupportedFunctionTypesContaining(functionType);
        if (localClusters == null || localClusters.isEmpty()) {
            List<LocalCluster> workers2 = workerRepository.findAll();
            if(workers2.isEmpty()) {
                throw new InternalServiceException("No worker found!");
            }
            return workers2.get(random.nextInt(workers2.size()));
        }
        return localClusters.get(random.nextInt(localClusters.size()));
    }

    private KubernetesClient getKubernetesClient(LocalCluster localCluster) {
        String masterUrl = "https://" + localCluster.getIpAddress() + ":" + localCluster.getKubernetesApiPort();
        Config config = new ConfigBuilder()
                .withMasterUrl(masterUrl)
                .withTrustCerts(true)
                .withOauthToken(localCluster.getKubernetesBearerToken())
                .build();
        return new KubernetesClientBuilder().withConfig(config).build();
    }
}
