package de.lenneflow.workerservice.dto;

import de.lenneflow.workerservice.enums.CloudProvider;
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
@Schema(name = "UnmanagedCluster")
public class UnmanagedClusterDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String clusterName;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String region;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> supportedFunctionTypes = new ArrayList<>();

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiServerEndpoint;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String caCertificate;

    @Schema(enumAsRef = true, requiredMode = Schema.RequiredMode.REQUIRED)
    private CloudProviderDto cloudProvider;

    @Schema(description = "Required by Cloud Cluster", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String cloudCredentialUid;

}
