package de.lenneflow.workflowservice.dto;

import de.lenneflow.workflowservice.enums.ControlStructure;
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

    private Integer retryCount = 0;

    private String workflowId;

    private String description;

    private ControlStructure controlStructure = ControlStructure.SUB_WORKFLOW;

    private String subWorkflowId;
}
