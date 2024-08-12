package de.lenneflow.workflowservice.model;

import de.lenneflow.workflowservice.enums.WorkFlowStepType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
@CompoundIndex(def = "{'workflowName': 1, 'stepName': 1}", unique = true)
public class WorkflowStep {

    @Id
    private String uid;

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

    //private String errorMessage;

    private String functionName;

    private Map<String, String> decisionCases = new LinkedHashMap<>();

    private Map<String, Object> inputData = new LinkedHashMap<>();

    private Integer retryCount;

    private LocalDateTime creationTime;

    private LocalDateTime updateTime;
}
