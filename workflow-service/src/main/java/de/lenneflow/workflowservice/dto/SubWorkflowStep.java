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
public class SubWorkflowStep {


    @Schema(name = "Workflow Step Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(name = "Workflow UID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String workflowUid;

    @Schema(name = "Description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(name = "Execution Order", requiredMode = Schema.RequiredMode.REQUIRED)
    private int executionOrder;

    @Schema(name = "Count of Retries", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer retryCount = 0;

    @Schema(name = "Sub Workflow UID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String subWorkflowUid;

    @Schema(name = "Input Data", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> inputData = new LinkedHashMap<>();
}
