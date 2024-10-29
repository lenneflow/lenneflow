package de.lenneflow.functionservice.feignmodels;

import de.lenneflow.functionservice.enums.CloudProvider;
import de.lenneflow.functionservice.enums.ClusterStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the cluster data transfer object used by the feign controller to retrieve a cluster object from the worker service.
 * @author Idrissa Ganemtore
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KubernetesCluster {

    private String uid;

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

    private List<Integer> usedHostPorts = new ArrayList<>();

    private String kubernetesAccessTokenUid;

    private String ingressServiceName;

    private String serviceUser;

    private String hostUrl;

    private boolean managed;

}
