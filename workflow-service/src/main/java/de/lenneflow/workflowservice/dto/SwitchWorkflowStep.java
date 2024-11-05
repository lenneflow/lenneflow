package de.lenneflow.workflowservice.dto;

import de.lenneflow.workflowservice.enums.ControlStructure;
import de.lenneflow.workflowservice.model.DecisionCase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SwitchWorkflowStep {


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

    @Schema(example = "([step2.output.randomValue] * 5 ) >= 10", requiredMode = Schema.RequiredMode.REQUIRED)
    private String switchCase; //example {stepname.outputData.field.field} > 10 ; will be validated by creation

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<DecisionCase> decisionCases = new ArrayList<>();

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> inputData = new LinkedHashMap<>();

}
