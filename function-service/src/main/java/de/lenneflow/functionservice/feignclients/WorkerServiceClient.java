package de.lenneflow.functionservice.feignclients;


import de.lenneflow.functionservice.feignmodels.ApiCredential;
import de.lenneflow.functionservice.feignmodels.KubernetesCluster;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "worker-service", url = "http://localhost:47003")
//@FeignClient(name = "worker-service")
public interface WorkerServiceClient {

    @GetMapping("/api/workers/clusters/{id}")
    KubernetesCluster getKubernetesClusterById(@PathVariable String id);

    @GetMapping("/api/workers/clusters")
    List<KubernetesCluster> getKubernetesClusterList();

    @PostMapping("/clusters/{uid}/update-used-port")
    KubernetesCluster updateUsedPorts(@PathVariable("uid") String clusterUid, @RequestBody List<Integer> usedPorts);

    @PostMapping("/clusters/api-credential/{uid}")
    ApiCredential getApiCredential(@PathVariable("uid") String apiCredentialUid);

}
