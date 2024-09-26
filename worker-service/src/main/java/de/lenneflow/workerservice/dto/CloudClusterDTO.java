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

    private String description;

    private CloudProvider cloudProvider;

    private String region;

    private boolean create;

    private String roleArn;

    private String securityGroupId;

    private List<String> subnetIds = new ArrayList<>();

    private List<String> supportedFunctionTypes = new ArrayList<>();

    private String cloudCredentialUid;

}
