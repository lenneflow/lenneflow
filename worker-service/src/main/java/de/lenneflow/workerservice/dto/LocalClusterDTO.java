package de.lenneflow.workerservice.dto;

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
@Schema(name = "LocalCluster")
public class LocalClusterDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String clusterName;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> supportedFunctionTypes = new ArrayList<>();

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiServerEndpoint;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String caCertificate;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String kubernetesAccessTokenUid;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String hostUrl;
}
