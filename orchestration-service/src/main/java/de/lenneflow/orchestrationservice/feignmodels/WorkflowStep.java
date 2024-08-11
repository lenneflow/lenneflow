package de.lenneflow.orchestrationservice.feignmodels;

import de.lenneflow.orchestrationservice.enums.FunctionStatus;
import de.lenneflow.orchestrationservice.enums.WorkFlowStepType;
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
public class WorkflowStep {

    //private String uid;

    private String stepName;

    private String workflowName;

    private String workflowId;

    private String description;

    private WorkFlowStepType workFlowStepType;

    private boolean retriable;

    private String nextStepId;

    private String nextStepName;

    private String previousStepId;

    private String previousStepName;

    private String errorMessage;

    private String functionName;

    private FunctionStatus functionStatus;

    private Map<String, String> decisionCases = new LinkedHashMap<>();

    private Map<String, Object> inputData = new LinkedHashMap<>();

    private Integer retryCount;

    private LocalDateTime creationTime;

    private LocalDateTime updateTime;
}
