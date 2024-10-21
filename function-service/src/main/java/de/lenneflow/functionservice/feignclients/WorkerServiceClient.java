package de.lenneflow.functionservice.feignclients;


import de.lenneflow.functionservice.feignmodels.ApiCredential;
import de.lenneflow.functionservice.feignmodels.KubernetesCluster;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@FeignClient(name = "worker-service")
public interface WorkerServiceClient {

    @GetMapping("/api/workers/clusters/{id}")
    KubernetesCluster getKubernetesClusterById(@PathVariable String id);

    @GetMapping("/api/workers/clusters")
    List<KubernetesCluster> getKubernetesClusterList();

    @PostMapping("/api/workers/clusters/{uid}/update-used-ports")
    KubernetesCluster updateUsedPorts(@PathVariable("uid") String clusterUid, @RequestBody List<Integer> usedPorts);

    @GetMapping("/api/workers/clusters/{uid}/api-credential")
    ApiCredential getApiCredential(@PathVariable("uid") String clusterUid);

}
