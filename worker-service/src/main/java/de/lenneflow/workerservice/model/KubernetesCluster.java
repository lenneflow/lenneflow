package de.lenneflow.workerservice.model;

import de.lenneflow.workerservice.enums.CloudProvider;
import de.lenneflow.workerservice.enums.ClusterStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
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

    @Indexed(unique = true)
    private String clusterName;

    private String hostName;

    private String description;

    private CloudProvider cloudProvider;

    private ClusterStatus status;

    private String ingressServiceName;

    private String region;

    private boolean create;

    private String cloudCredentialUid;

    private String kubernetesCredentialUid;

    @Reference
    private LocalClusterDetails localClusterDetails;

    private String roleArn;

    private String securityGroupId;

    private List<String> subnetIds = new ArrayList<>();

    private List<String> supportedFunctionTypes = new ArrayList<>();

    private List<Integer> usedHostPorts = new ArrayList<>();

    private LocalDateTime created;

    private LocalDateTime updated;

}
