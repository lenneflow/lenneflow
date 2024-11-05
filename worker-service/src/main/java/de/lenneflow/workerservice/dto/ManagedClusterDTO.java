package de.lenneflow.workerservice.dto;

import de.lenneflow.workerservice.enums.CloudProvider;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "ManagedCluster")
public class ManagedClusterDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String clusterName;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String region;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String kubernetesVersion;

    @Schema(enumAsRef = true, requiredMode = Schema.RequiredMode.REQUIRED)
    private CloudProvider cloudProvider;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int desiredNodeCount;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int minimumNodeCount;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int maximumNodeCount;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String instanceType;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String amiType;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> supportedFunctionTypes = new ArrayList<>();

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String cloudCredentialUid;


    //Hidden part. Only necessary to communicate with k8s API
    @Hidden
    private String accountId;

    @Hidden
    private String accessKey;

    @Hidden
    private String secretKey;

}
