package de.lenneflow.functionservice.feignmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KubernetesCluster {

    private String uid;

    private String clusterName;

    private String description;

    private String region;

    private boolean create;

    private String roleArn;

    private String securityGroupId;

    private List<String> subnetIds = new ArrayList<>();

    private List<String> supportedFunctionTypes = new ArrayList<>();

    private List<Integer> usedHostPorts = new ArrayList<>();

    private String cloudCredentialUid;

    private String apiCredentialUid;

    private String ingressServiceName;

    private String serviceUser;

    private String hostName;

}
