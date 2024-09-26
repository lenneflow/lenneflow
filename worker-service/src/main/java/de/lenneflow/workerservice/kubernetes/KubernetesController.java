package de.lenneflow.workerservice.kubernetes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.lenneflow.workerservice.enums.DeploymentState;
import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.feignclients.FunctionServiceClient;
import de.lenneflow.workerservice.feignmodel.Function;
import de.lenneflow.workerservice.model.KubernetesCluster;
import de.lenneflow.workerservice.repository.KubernetesClusterRepository;
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

//@Component
public class KubernetesController {

//    public static final String NAMESPACE = "lenneflow";
//    public static final String SERVICE_ACCOUNT_NAME = "lenneflow-sa";
//
//    private final Random random = new Random();
//
//    final KubernetesClusterRepository kubernetesClusterRepository;
//    final FunctionServiceClient functionServiceClient;
//
//    public KubernetesController(KubernetesClusterRepository kubernetesClusterRepository, FunctionServiceClient functionServiceClient) {
//        this.kubernetesClusterRepository = kubernetesClusterRepository;
//        this.functionServiceClient = functionServiceClient;
//    }
//
//    public void checkWorkerConnection(KubernetesCluster kubernetesCluster) {
//        KubernetesClient client = getKubernetesClient(kubernetesCluster);
//        String apiVersion = client.getApiVersion();
//        client.close();
//        if(apiVersion == null || apiVersion.isEmpty()){
//            throw new InternalServiceException("The connection to the kubernetesCluster " + kubernetesCluster.getClusterName() + " was not possible");
//        }
//    }
//
//    public void checkServiceExists(KubernetesCluster kubernetesCluster) {
//        KubernetesClient client = getKubernetesClient(kubernetesCluster);
//        String apiVersion = client.getApiVersion();
//        client.close();
//        if(apiVersion == null || apiVersion.isEmpty()){
//            throw new InternalServiceException("The connection to the kubernetesCluster " + kubernetesCluster.getClusterName() + " was not possible");
//        }
//    }
//
//    public void deployFunctionImageToWorker(Function function) {
//        KubernetesCluster kubernetesCluster = getWorkerForFunction(function);
//        assignHostPortToFunction(kubernetesCluster, function);
//        KubernetesClient client = getKubernetesClient(kubernetesCluster);
//        createNamespace(kubernetesCluster);
//        createServiceAccount(kubernetesCluster);
//        Deployment deployment = YamlEditor.createKubernetesDeploymentResource(function,2, SERVICE_ACCOUNT_NAME);
//        Service service = YamlEditor.createKubernetesServiceResource(function, "ClusterIP");
//        client.resource(deployment).inNamespace(NAMESPACE).create();
//        client.resource(service).inNamespace(NAMESPACE).create();
//        createOrUpdateIngress(kubernetesCluster,function);
//        String functionServiceUrl = "https://" + kubernetesCluster.getClusterConnectionDetails().getHostName() + function.getResourcePath();
//        function.setServiceUrl(functionServiceUrl);
//        functionServiceClient.updateFunction(function, function.getUid());
//        updateDeploymentState(kubernetesCluster, function);
//    }
//
//    private void updateDeploymentState(KubernetesCluster kubernetesCluster, Function function) {
//        new Thread(() ->{
//            try {
//                Thread.sleep(15000);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//            updateFunction(function, DeploymentState.DEPLOYING);
//            KubernetesClient client = getKubernetesClient(kubernetesCluster);
//            String deploymentName = function.getName();
//
//            client.apps().deployments().inNamespace(NAMESPACE).withName(deploymentName).waitUntilCondition(
//                    d -> ( d.getStatus().getReadyReplicas() > 0), 5, MINUTES);
//            if(client.apps().deployments().inNamespace(NAMESPACE).withName(deploymentName).isReady()){
//                updateFunction(function, DeploymentState.DEPLOYED);
//                return;
//            }
//            updateFunction(function, DeploymentState.FAILED);
//        }).start();
//    }
//
//    private void updateFunction(Function function, DeploymentState deploymentState) {
//        function.setDeploymentState(deploymentState);
//        functionServiceClient.updateFunction(function, function.getUid());
//    }
//
//    public void assignHostPortToFunction(KubernetesCluster kubernetesCluster, Function function){
//        if(function.getAssignedHostPort() >= 47000){
//            return;
//        }
//        List<Integer> ports = kubernetesCluster.getUsedHostPorts();
//        if(ports.isEmpty()){
//            ports.add(47000);
//        }
//        int nextPort = Collections.max(ports) + 1;
//        function.setAssignedHostPort(nextPort);
//        functionServiceClient.updateFunction(function, function.getUid());
//        ports.add(nextPort);
//        kubernetesCluster.setUsedHostPorts(ports);
//        kubernetesClusterRepository.save(kubernetesCluster);
//    }
//
//    public void deployFunctionToWorker(Function function, List<String> deploymentFileUrls) {
//        try {
//            KubernetesCluster kubernetesCluster = getWorkerForFunction(function);
//            KubernetesClient client = getKubernetesClient(kubernetesCluster);
//            createNamespace(kubernetesCluster);
//
//            Yaml yaml = new Yaml();
//            ObjectMapper mapper = new YAMLMapper();
//            for (String deploymentFileUrl : deploymentFileUrls) {
//                URL url = new URL(deploymentFileUrl);
//                URLConnection connection = url.openConnection();
//
//                Map<String, Object> obj = yaml.load(connection.getInputStream());
//                String resource = mapper.writeValueAsString(obj);
//                client.resource(resource).inNamespace(NAMESPACE).create();
//            }
//        } catch (Exception e) {
//            throw new InternalServiceException(e.getMessage());
//        }
//    }
//
//    private void createOrUpdateIngress(KubernetesCluster kubernetesCluster, Function function) {
//        KubernetesClient client = getKubernetesClient(kubernetesCluster);
//        Ingress currentIngress = client.network().v1().ingresses().inNamespace(NAMESPACE).withName(kubernetesCluster.getClusterConnectionDetails().getIngressServiceName()).get();
//        assignHostPortToFunction(kubernetesCluster, function);
//        try {
//            if (currentIngress == null) {
//                Ingress ingressResource = YamlEditor.createKubernetesIngressResource(kubernetesCluster, function);
//                currentIngress = client.resource(ingressResource).inNamespace(NAMESPACE).create();
//                return;
//            }
//            assert currentIngress != null;
//            Ingress updatedIngressResource = YamlEditor.addPathToKubernetesIngressResource(currentIngress, function);
//            client.resource(updatedIngressResource).inNamespace(NAMESPACE).patch();
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new InternalServiceException("It was not possible to create the ingress service ");
//        }
//
//    }
//
//    private void createNamespace(KubernetesCluster kubernetesCluster) {
//        KubernetesClient client = getKubernetesClient(kubernetesCluster);
//        Namespace ns = new NamespaceBuilder().withNewMetadata().withName(NAMESPACE)
//                .endMetadata().build();
//        try {
//            if (client.namespaces().withName(NAMESPACE).get() != null) {
//                return;
//            }
//            client.namespaces().resource(ns).create();
//        } catch (Exception e) {
//            throw new InternalServiceException("It was not possible to create the namespace " + NAMESPACE + "\n" + e.getMessage());
//        }
//    }
//
//    private void createServiceAccount(KubernetesCluster kubernetesCluster) {
//        KubernetesClient client = getKubernetesClient(kubernetesCluster);
//        try {
//            if (client.serviceAccounts().inNamespace(NAMESPACE).withName(SERVICE_ACCOUNT_NAME).get() == null) {
//                ServiceAccount serviceAccountResource = YamlEditor.createKubernetesServiceAccountResource(SERVICE_ACCOUNT_NAME);
//                client.resource(serviceAccountResource).inNamespace(NAMESPACE).create();
//                ClusterRole roleResource = YamlEditor.createKubernetesClusterRoleResource(SERVICE_ACCOUNT_NAME, NAMESPACE);
//                client.resource(roleResource).inNamespace(NAMESPACE).create();
//                ClusterRoleBinding bindingResource = YamlEditor.createKubernetesClusterRoleBindingResource(SERVICE_ACCOUNT_NAME, NAMESPACE);
//                client.resource(bindingResource).inNamespace(NAMESPACE).create();
//            }
//        } catch (Exception e) {
//            throw new InternalServiceException("It was not possible to create the service account " + SERVICE_ACCOUNT_NAME + "\n" + e.getMessage());
//        }
//    }
//
//    private KubernetesCluster getWorkerForFunction(Function function) {
//        String functionType = function.getType();
//        List<KubernetesCluster> kubernetesClusters = kubernetesClusterRepository.findBySupportedFunctionTypesContaining(functionType);
//        if (kubernetesClusters == null || kubernetesClusters.isEmpty()) {
//            List<KubernetesCluster> workers2 = kubernetesClusterRepository.findAll();
//            if(workers2.isEmpty()) {
//                throw new InternalServiceException("No worker found!");
//            }
//            return workers2.get(random.nextInt(workers2.size()));
//        }
//        return kubernetesClusters.get(random.nextInt(kubernetesClusters.size()));
//    }
//
//    private KubernetesClient getKubernetesClient(KubernetesCluster kubernetesCluster) {
//        ClusterConnectionDetails connectionDetails = kubernetesCluster.getClusterConnectionDetails();
//        Config config = new ConfigBuilder()
//                .withMasterUrl(connectionDetails.getApiServerEndpoint())
//                .withTrustCerts(true)
//                .withOauthToken(connectionDetails.getSessionToken())
//                .build();
//        return new KubernetesClientBuilder().withConfig(config).build();
//    }
}
