package de.lenneflow.functionservice.feignclients;


import de.lenneflow.functionservice.feignmodels.AccessToken;
import de.lenneflow.functionservice.feignmodels.KubernetesCluster;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * This is the feign client class for the worker service. This class acts as the interface between the function service and the
 * worker service. It will make rest calls to the worker service and return the responses.
 * @author Idrissa Ganemtore
 */
@FeignClient(name = "worker-service")
public interface WorkerServiceClient {

    @GetMapping("/api/workers/cluster/{uid}")
    KubernetesCluster getKubernetesClusterById(@PathVariable String uid);

    @GetMapping("/api/workers/clusters")
    List<KubernetesCluster> getKubernetesClusterList();

    @PostMapping("/api/workers/cluster/{uid}/update-used-ports")
    KubernetesCluster updateUsedPorts(@PathVariable("uid") String clusterUid, @RequestBody List<Integer> usedPorts);

    @GetMapping("/api/workers/cluster/{uid}/connection-token")
    AccessToken getK8sConnectionToken(@PathVariable("uid") String clusterUid);

}
