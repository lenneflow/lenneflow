package de.lenneflow.workflowservice.dto;

import de.lenneflow.workflowservice.enums.WorkFlowStepType;
import de.lenneflow.workflowservice.model.WorkflowStep;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SwitchWorkflowStep {


    @Hidden
    private String uid;

    private String stepName;

    private String workflowUid;

    private String description;

    private WorkFlowStepType workFlowStepType = WorkFlowStepType.SWITCH;

    private int executionOrder;

    private String switchCondition; //example {stepname.outputData.field.field} > 10 ; will be validated by creation

    private Map<String, WorkflowStep> decisionCases = new LinkedHashMap<>();

    private Map<String, Object> inputData = new LinkedHashMap<>();

    @Hidden
    private LocalDateTime creationTime;

    @Hidden
    private LocalDateTime updateTime;


}
