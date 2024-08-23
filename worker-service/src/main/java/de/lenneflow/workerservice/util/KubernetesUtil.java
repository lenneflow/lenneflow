package de.lenneflow.workerservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.lenneflow.workerservice.enums.DeploymentState;
import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.feignclients.FunctionServiceClient;
import de.lenneflow.workerservice.feignmodel.Function;
import de.lenneflow.workerservice.model.Worker;
import de.lenneflow.workerservice.repository.WorkerRepository;
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
import org.yaml.snakeyaml.Yaml;

import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.util.concurrent.TimeUnit.MINUTES;

@Component
public class KubernetesUtil {

    public static final String NAMESPACE = "lenneflow";
    public static final String SERVICE_ACCOUNT_NAME = "lenneflow-sa";

    final
    WorkerRepository workerRepository;
    final FunctionServiceClient functionServiceClient;

    public KubernetesUtil(WorkerRepository workerRepository, FunctionServiceClient functionServiceClient) {
        this.workerRepository = workerRepository;
        this.functionServiceClient = functionServiceClient;
    }

    public void checkWorkerConnection(Worker worker) {
        KubernetesClient client = getKubernetesClient(worker);
        String apiVersion = client.getApiVersion();
        client.close();
        if(apiVersion == null || apiVersion.isEmpty()){
            throw new InternalServiceException("The connection to the worker " + worker.getName() + " was not possible");
        }
    }

    public void deployFunctionImageToWorker(Function function) {
        Worker worker = getWorkerForFunction(function);
        assignHostPortToFunction(worker, function);
        KubernetesClient client = getKubernetesClient(worker);
        createNamespace(worker);
        createServiceAccount(worker);
        Deployment deployment = YamlEditor.createKubernetesDeploymentResource(function,2, SERVICE_ACCOUNT_NAME);
        Service service = YamlEditor.createKubernetesServiceResource(function, "ClusterIP");
        client.resource(deployment).inNamespace(NAMESPACE).create();
        client.resource(service).inNamespace(NAMESPACE).create();
        createOrUpdateIngress(worker,function);
        updateDeploymentState(worker, function);

    }

    private void updateDeploymentState(Worker worker, Function function) {
        new Thread(() ->{
            updateFunction(function, DeploymentState.DEPLOYING);
            KubernetesClient client = getKubernetesClient(worker);
            String deploymentName = function.getName();
            client.apps().deployments().inNamespace(NAMESPACE).withName(deploymentName).waitUntilCondition(
                    pod -> pod.getStatus().getReadyReplicas() > 0, 5, MINUTES);
            if(client.pods().inNamespace(NAMESPACE).withName(deploymentName).isReady()){
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

    public void assignHostPortToFunction(Worker worker, Function function){
        if(function.getAssignedHostPort() >= 47000){
            return;
        }
        List<Integer> ports = worker.getUsedHostPorts();
        if(ports.isEmpty()){
            ports.add(47000);
        }
        int nextPort = Collections.max(ports) + 1;
        function.setAssignedHostPort(nextPort);
        functionServiceClient.updateFunction(function, function.getUid());
        ports.add(nextPort);
        worker.setUsedHostPorts(ports);
        workerRepository.save(worker);
    }

    public void deployFunctionToWorker(Function function, List<String> deploymentFileUrls) {
        try {
            Worker worker = getWorkerForFunction(function);
            KubernetesClient client = getKubernetesClient(worker);
            createNamespace(worker);

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

    private void createOrUpdateIngress(Worker worker, Function function) {
        KubernetesClient client = getKubernetesClient(worker);
        Ingress currentIngress = client.network().v1().ingresses().inNamespace(NAMESPACE).withName(worker.getIngressServiceName()).get();
        assignHostPortToFunction(worker, function);
        try {
            if (currentIngress == null) {
                Ingress ingressResource = YamlEditor.createKubernetesIngressResource(worker, function);
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

    private void createNamespace(Worker worker) {
        KubernetesClient client = getKubernetesClient(worker);
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

    private void createServiceAccount(Worker worker) {
        KubernetesClient client = getKubernetesClient(worker);
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

    private Worker getWorkerForFunction(Function function) {
        Random random = new Random();
        String functionType = function.getType();
        List<Worker> workers = workerRepository.findBySupportedFunctionTypesContaining(functionType);
        if (workers == null || workers.isEmpty()) {
            List<Worker> workers2 = workerRepository.findAll();
            if(workers2.isEmpty()) {
                throw new InternalServiceException("No worker found!" );
            }
            return workers2.get(random.nextInt(workers2.size()));
        }
        return workers.get(random.nextInt(workers.size()));
    }

    private KubernetesClient getKubernetesClient(Worker worker) {
        String masterUrl = "https://" + worker.getIpAddress() + ":" + worker.getKubernetesApiPort();
        Config config = new ConfigBuilder()
                .withMasterUrl(masterUrl)
                .withTrustCerts(true)
                .withOauthToken(worker.getKubernetesBearerToken())
                .build();
        return new KubernetesClientBuilder().withConfig(config).build();
    }
}
