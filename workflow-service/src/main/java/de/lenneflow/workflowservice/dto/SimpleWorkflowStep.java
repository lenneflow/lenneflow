package de.lenneflow.workflowservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleWorkflowStep {


    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String workflowUid;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int executionOrder;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer retryCount = 0;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String functionUid;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> inputData = new LinkedHashMap<>();

}
