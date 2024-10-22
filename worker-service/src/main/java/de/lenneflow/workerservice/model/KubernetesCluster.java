package de.lenneflow.workerservice.model;

import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.enums.ClusterStatus;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
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

    //DTO Section

    private String clusterName;

    private String region;

    private String description;

    private String kubernetesVersion;

    private CloudProvider cloudProvider;

    private int desiredNodeCount;

    private int minimumNodeCount;

    private int maximumNodeCount;

    private String instanceType;

    private String amiType;

    private String apiServerEndpoint;

    private String caCertificate;

    private List<String> supportedFunctionTypes = new ArrayList<>();

    private ClusterStatus status;

    private String cloudCredentialUid;



    //Non DTO Section

    @Hidden
    private List<Integer> usedHostPorts = new ArrayList<>();

    private String kubernetesAccessTokenUid;

    private String ingressServiceName;

    private String serviceUser;

    private String hostName;

    private LocalDateTime created;

    private LocalDateTime updated;

    private boolean managed;

}
