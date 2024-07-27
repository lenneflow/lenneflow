package de.lenneflow.workflowservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.lenneflow.workflowservice.enums.WorkFlowStepType;
import de.lenneflow.workflowservice.enums.WorkflowStepStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class WorkflowStep {

    private String uid;

    private String stepName;

    private String workflowId;

    private String description;

    private WorkFlowStepType workFlowStepType;

    private boolean retriable;

    private String nextStepId;

    private String previousStepId;

    private String errorMessage;

    private String taskId;

    private Map<String, String> decisionCases = new LinkedHashMap<>();

    private Integer retryCount;

    private LocalDateTime creationTime;

    private LocalDateTime updateTime;
}
