package de.lenneflow.workerservice.component;

import de.lenneflow.workerservice.dto.ManagedClusterDTO;
import de.lenneflow.workerservice.dto.NodeGroupDTO;
import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.model.AccessToken;
import de.lenneflow.workerservice.model.KubernetesCluster;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CloudClusterController {

    @Value("${k8sapi.address}")
    private String k8sApiRootEndpoint;

    private final RestTemplate restTemplate;

    public CloudClusterController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public HttpStatusCode createCluster(ManagedClusterDTO clusterDTO) {
        String createClusterUrl = k8sApiRootEndpoint + "/cluster/create";
        ResponseEntity<Void> response = restTemplate.exchange(createClusterUrl, HttpMethod.POST, new HttpEntity<>(clusterDTO), Void.class);
        return  response.getStatusCode();
    }

    public KubernetesCluster getCluster(String clusterName, CloudProvider cloudProvider, String region) {
        String getClusterUrl = k8sApiRootEndpoint + "/cluster/" + clusterName+ "/provider/"+ cloudProvider.toString() +  "/region/" + region;
        ResponseEntity<KubernetesCluster> response = restTemplate.exchange(getClusterUrl, HttpMethod.GET, null, KubernetesCluster.class);
        return response.getBody();
    }

    public AccessToken getConnectionToken(String clusterName, CloudProvider cloudProvider, String region) {
        String getClusterUrl = k8sApiRootEndpoint + "/access-token/cluster/" + clusterName + "/provider/" + cloudProvider.toString() + "/region/" + region;
        ResponseEntity<AccessToken> response = restTemplate.exchange(getClusterUrl, HttpMethod.GET, null, AccessToken.class);
        return response.getBody();
    }

    public HttpStatusCode updateNodeGroup(NodeGroupDTO nodeGroupDTO) {
        String createClusterUrl = k8sApiRootEndpoint + "/cluster/update";
        ResponseEntity<Void> response = restTemplate.exchange(createClusterUrl, HttpMethod.POST, new HttpEntity<>(nodeGroupDTO), Void.class);
        return  response.getStatusCode();
    }

    public HttpStatusCode deleteCluster(String clusterName, CloudProvider cloudProvider, String region) {
        String deleteClusterUrl = k8sApiRootEndpoint + "/cluster/" + clusterName+ "/provider/"+ cloudProvider.toString() +  "/region/" + region;
        ResponseEntity<Void> response = restTemplate.exchange(deleteClusterUrl, HttpMethod.DELETE, null, Void.class);
        return  response.getStatusCode();
    }

    public void deleteAllResourcesInNamespace(KubernetesCluster kubernetesCluster, String namespace) {
        KubernetesClient client = getKubernetesClient(kubernetesCluster);
        client.apps().deployments().inNamespace(namespace).delete();
        client.services().inNamespace(namespace).delete();
    }

    /**
     * Creates a kubernetes client using the credentials.
     * @param kubernetesCluster the cluster
     * @return the kubernetes client Object
     */
    private KubernetesClient getKubernetesClient(KubernetesCluster kubernetesCluster) {
        AccessToken token = getConnectionToken(kubernetesCluster.getClusterName(), kubernetesCluster.getCloudProvider(), kubernetesCluster.getRegion());
        String  masterUrl = kubernetesCluster.getApiServerEndpoint();

        Config config = new ConfigBuilder()
                .withMasterUrl(masterUrl)
                .withTrustCerts(true)
                .withOauthToken(token.getToken())
                .build();
        return new KubernetesClientBuilder().withConfig(config).build();
    }


}
