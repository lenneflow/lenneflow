package de.lenneflow.workerservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.lenneflow.workerservice.enums.DeploymentState;
import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.feignclients.FunctionServiceClient;
import de.lenneflow.workerservice.feignmodel.Function;
import de.lenneflow.workerservice.model.Worker;
import de.lenneflow.workerservice.repository.WorkerRepository;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
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
        if(apiVersion == null && apiVersion.isEmpty()){
            throw new InternalServiceException("The connection to the worker " + worker.getName() + " was not possible");
        }
    }

    public void deployFunctionImageToWorker(Function function) {
        Worker worker = getWorkerForFunction(function);
        int hostPort = getNextFreeHostPort(worker);
        KubernetesClient client = getKubernetesClient(worker);
        createNamespace(worker);
        createServiceAccount(worker);
        Deployment deployment = YamlEditor.createKubernetesDeploymentResource(function,2, SERVICE_ACCOUNT_NAME,hostPort);
        Service service = YamlEditor.createKubernetesServiceResource(function, "ClusterIP", hostPort);
        client.resource(deployment).inNamespace(NAMESPACE).create();
        client.resource(service).inNamespace(NAMESPACE).create();
        createOrUpdateIngress(worker,function);
        updateDeploymentState(worker, function, 5);

    }

    private void updateDeploymentState(Worker worker, Function function, int waitTimeInMinutes) {
        new Thread(() ->{
            updateFunction(function, DeploymentState.DEPLOYING);
            KubernetesClient client = getKubernetesClient(worker);
            String realPodName = function.getName() + "_"; //TODO get pod name
            client.pods().inNamespace(NAMESPACE).withName(realPodName).waitUntilCondition(
                    pod -> pod.getStatus().getContainerStatuses().stream().allMatch(
                            containerStatus -> containerStatus.getReady() && containerStatus.getStarted()), waitTimeInMinutes, MINUTES);
            if(client.pods().inNamespace(NAMESPACE).withName(realPodName).isReady()){
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

    public int getNextFreeHostPort(Worker worker){
        List<Integer> ports = worker.getUsedHostPorts();
        if(ports.isEmpty()){
            ports.add(4000);
            worker.setUsedHostPorts(ports);
            workerRepository.save(worker);
            return ports.get(0);
        }
        int nextPort = Collections.max(ports) + 1;
        ports.add(nextPort);
        worker.setUsedHostPorts(ports);
        workerRepository.save(worker);
        return nextPort;
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
        String currentIngressResource = worker.getCurrentIngress();
        int hostPort = getNextFreeHostPort(worker);
        try {
            if (currentIngressResource == null || currentIngressResource.isEmpty()) {
                Ingress ingressResource = YamlEditor.createKubernetesIngressResource(worker, function,hostPort);
                client.resource(ingressResource).inNamespace(NAMESPACE).create();
                worker.setCurrentIngress(ingressResource.toString());
                workerRepository.save(worker);
                return;
            }
            ObjectMapper mapper = new YAMLMapper();
            Ingress currentIngress = mapper.readValue(currentIngressResource, Ingress.class);
            Ingress updatedIngressResource = YamlEditor.addPathToKubernetesIngressResource(currentIngress, function, hostPort);
            client.resource(updatedIngressResource).inNamespace(NAMESPACE).create();
            worker.setCurrentIngress(updatedIngressResource.toString());
            workerRepository.save(worker);
        } catch (Exception e) {
            throw new InternalServiceException("It was not possible to create the ingress service ");
        }

    }

    private void createNamespace(Worker worker) {
        KubernetesClient client = getKubernetesClient(worker);
        try {
            if (client.namespaces().withName(NAMESPACE).isReady()) {
                return;
            }
            client.namespaces().withName(NAMESPACE).create();
        } catch (Exception e) {
            throw new InternalServiceException("It was not possible to create the namespace " + NAMESPACE);
        }
    }

    private void createServiceAccount(Worker worker) {
        KubernetesClient client = getKubernetesClient(worker);
        try {
            if (client.serviceAccounts().withName(SERVICE_ACCOUNT_NAME).isReady()) {
                return;
            }
            ServiceAccount serviceAccountResource = YamlEditor.createKubernetesServiceAccountResource(SERVICE_ACCOUNT_NAME);
            client.resource(serviceAccountResource).inNamespace(NAMESPACE).create();
            ClusterRole roleResource = YamlEditor.createKubernetesClusterRoleResource(SERVICE_ACCOUNT_NAME, NAMESPACE);
            client.resource(roleResource).inNamespace(NAMESPACE).create();
            ClusterRoleBinding bindingResource = YamlEditor.createKubernetesClusterRoleBindingResource(SERVICE_ACCOUNT_NAME, NAMESPACE);
            client.resource(bindingResource).inNamespace(NAMESPACE).create();
        } catch (Exception e) {
            throw new InternalServiceException("It was not possible to create the service account " + SERVICE_ACCOUNT_NAME);
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
