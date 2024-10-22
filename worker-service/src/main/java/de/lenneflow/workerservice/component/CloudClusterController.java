package de.lenneflow.workerservice.component;

import de.lenneflow.workerservice.dto.ManagedClusterDTO;
import de.lenneflow.workerservice.dto.NodeGroupDTO;
import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.model.AccessToken;
import de.lenneflow.workerservice.model.KubernetesCluster;
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

    public AccessToken getAccessToken(KubernetesCluster kubernetesCluster) {
        String accessTokenUrl = k8sApiRootEndpoint + "/access-token/provider/"+ kubernetesCluster.getCloudProvider().toString() + "/cluster/" + kubernetesCluster.getClusterName() + "/region/" +kubernetesCluster.getRegion();
        return restTemplate.getForObject(accessTokenUrl, AccessToken.class);
    }

    public HttpStatusCode createCluster(ManagedClusterDTO clusterDTO) {
        String createClusterUrl = k8sApiRootEndpoint + "/cluster/create";
        ResponseEntity<Void> response = restTemplate.exchange(createClusterUrl, HttpMethod.POST, new HttpEntity<>(clusterDTO), Void.class);
        return  response.getStatusCode();
    }

    public KubernetesCluster getCluster(String clusterName, CloudProvider cloudProvider, String region) {
        String getClusterUrl = k8sApiRootEndpoint + "/cluster/" + clusterName+ "/provider/"+ cloudProvider.toString() +  "/region/" + region;
        return restTemplate.getForObject(getClusterUrl, KubernetesCluster.class);
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

}
