package de.lenneflow.workflowservice.dto;

import de.lenneflow.workflowservice.enums.ControlStructure;
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

    private String name;

    private int executionOrder;

    private Integer retryCount = 0;

    private String workflowId;

    private String description;

    private ControlStructure controlStructure = ControlStructure.SUB_WORKFLOW;

    private String subWorkflowId;

    private Map<String, Object> inputData = new LinkedHashMap<>();
}
