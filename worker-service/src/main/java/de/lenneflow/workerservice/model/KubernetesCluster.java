package de.lenneflow.workerservice.model;

import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.enums.ClusterStatus;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class KubernetesCluster {

    @Id
    private String uid;

    private String clusterName;

    private String description;

    private CloudProvider cloudProvider;

    private String apiServerEndpoint;

    private String kubernetesVersion;

    private ClusterStatus status;

    private String region;

    private boolean create;

    private List<String> supportedFunctionTypes = new ArrayList<>();

    @Hidden
    private List<Integer> usedHostPorts = new ArrayList<>();

    private String cloudCredentialUid;

    private String apiCredentialUid;

    private String ingressServiceName;

    private String serviceUser;

    private String hostName;

    private LocalDateTime created;

    private LocalDateTime updated;

}
