package de.lenneflow.workerservice.dto;

import de.lenneflow.workerservice.enums.CloudProvider;
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
public class CloudClusterDTO {

    private String clusterName;

    private String region;

    private String kubernetesVersion;

    private CloudProvider cloudProvider;

    private int desiredNodeCount;

    private int minimumNodeCount;

    private int maximumNodeCount;

    private String instanceType;

    private String amiType;

    private List<String> supportedFunctionTypes = new ArrayList<>();

    private String cloudCredentialUid;

}
