package de.lenneflow.workflowservice.dto;

import de.lenneflow.workflowservice.enums.WorkFlowStepType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubWorkflowStep {


    private String uid;

    private String stepName;

    private int executionOrder;

    private String workflowId;

    private String description;

    private WorkFlowStepType workFlowStepType = WorkFlowStepType.SUB_WORKFLOW;

    private String subWorkflowId;
}
