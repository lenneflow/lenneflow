package de.lenneflow.workflowservice.dto;

import de.lenneflow.workflowservice.enums.WorkFlowStepType;
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
public class WhileWorkflowStep {


    private String uid;

    private String stepName;

    private String workflowId;

    private String description;

    private WorkFlowStepType workFlowStepType = WorkFlowStepType.DO_WHILE;

    private int executionOrder;

    private String functionId;

    private Map<String, Object> inputData = new LinkedHashMap<>();

    private Integer retryCount;

    private LocalDateTime creationTime;

    private LocalDateTime updateTime;
}
