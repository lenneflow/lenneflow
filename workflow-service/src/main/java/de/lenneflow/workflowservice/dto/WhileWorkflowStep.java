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
public class WhileWorkflowStep {


    @Schema(name = "Workflow Step name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(name = "Workflow UID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String workflowUid;

    @Schema(name = "Description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(name = "Execution order", requiredMode = Schema.RequiredMode.REQUIRED)
    private int executionOrder;

    @Schema(name = "Count of retries", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer retryCount = 0;

    @Schema(name = "Function UID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String functionUid;

    @Schema(name = "Stop condition", example = "[step2.output.randomValue] < 10", requiredMode = Schema.RequiredMode.REQUIRED)
    private String stopCondition;

    @Schema(name = "Input data", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> inputData = new LinkedHashMap<>();

}
