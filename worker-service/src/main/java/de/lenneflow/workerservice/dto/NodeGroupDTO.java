package de.lenneflow.workerservice.dto;

import de.lenneflow.workerservice.enums.CloudProvider;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "NodeGroup")
public class NodeGroupDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String clusterUid;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int minimumNodeCount;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int maximumNodeCount;

    @Hidden
    private int desiredNodeCount;

    @Hidden
    private String clusterName;

    @Hidden
    private String region;

    @Hidden
    private CloudProvider cloudProvider;

}
