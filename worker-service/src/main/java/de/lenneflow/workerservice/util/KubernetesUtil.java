package de.lenneflow.workerservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.lenneflow.workerservice.exception.InternalServiceException;
import de.lenneflow.workerservice.feignmodel.Function;
import de.lenneflow.workerservice.model.Worker;
import de.lenneflow.workerservice.repository.WorkerRepository;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class KubernetesUtil {

    public static final String NAMESPACE = "lenneflow";

    final
    WorkerRepository workerRepository;

    public KubernetesUtil(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    public boolean checkWorkerConnection(Worker worker) {
        KubernetesClient client = getKubernetesClient(worker);
        String apiVersion = client.getApiVersion();
        client.close();
        return apiVersion != null && !apiVersion.isEmpty();
    }

    public boolean deployFunctionToKubernetes(Function function) {
        try {
            Worker worker = getWorkerForFunction(function);
            createNamespace(worker);

            Yaml yaml = new Yaml();
            ObjectMapper mapper = new YAMLMapper();
            String deploymentFileUrl = function.getDeploymentFileUrl();
            URL path = new URL(deploymentFileUrl);
            URLConnection connection = path.openConnection();

            KubernetesClient client = getKubernetesClient(worker);
            Map<String, Object> obj = yaml.load(connection.getInputStream());
            String resource = mapper.writeValueAsString(obj);
            client.resource(resource).inNamespace(NAMESPACE).create();
            return true;
        } catch (Exception e) {
            throw new InternalServiceException(e.getMessage());
        }
    }

    private boolean createNamespace(Worker worker) {
        KubernetesClient client = getKubernetesClient(worker);
        try {
            if (client.namespaces().withName(NAMESPACE).isReady()) {
                return true;
            }
            client.namespaces().withName(NAMESPACE).create();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Worker getWorkerForFunction(Function function) {
        Random random = new Random();
        String functionType = function.getType();
        List<Worker> workers = workerRepository.findBySupportedFunctionTypesContaining(functionType);
        if (workers == null || workers.isEmpty()) {
            List<Worker> workers2 = workerRepository.findAll();
            if(workers2.isEmpty()) {
                throw new InternalServiceException("No worker found " );
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
