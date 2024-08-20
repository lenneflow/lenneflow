package de.lenneflow.workerservice.util;

import de.lenneflow.workerservice.feignmodel.Function;
import de.lenneflow.workerservice.model.Worker;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

public class KubernetesUtil {

    public static final String NAMESPACE = "lenneflow";

    public static boolean checkApiConnection(Worker worker) {
        KubernetesClient client = getKubernetesClient(worker);
        String apiVersion = client.getApiVersion();
        client.close();
        return apiVersion != null && !apiVersion.isEmpty();
    }

    public static boolean createNamespace(Worker worker) {
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

    public static boolean deployResource(Worker worker, Function function) {
        KubernetesClient client = getKubernetesClient(worker);
        client.resource("").inNamespace(NAMESPACE).create();
        return true;
    }

    private static KubernetesClient getKubernetesClient(Worker worker) {
        String masterUrl = "https://" + worker.getIpAddress() + ":" + worker.getApiPort();
        Config config = new ConfigBuilder()
                .withMasterUrl(masterUrl)
                .withTrustCerts(true)
                .withOauthToken(worker.getBearerToken())
                .build();
        return new KubernetesClientBuilder().withConfig(config).build();
    }
}
