package de.lenneflow.workerservice.dto;

import de.lenneflow.workerservice.enums.CloudProvider;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NodeGroupDTO {

    private String clusterUid;

    private String description;

    private int minNodeCount;

    private int desiredNodeCount;

    private int maxNodeCount;

    @Hidden
    private String clusterName;

    @Hidden
    private String region;

    @Hidden
    private CloudProvider cloudProvider;


}
