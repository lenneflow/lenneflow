package de.lenneflow.orchestrationservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.lenneflow.orchestrationservice.enums.TaskStatus;
import de.lenneflow.orchestrationservice.enums.WorkFlowStepType;
import de.lenneflow.orchestrationservice.feignmodels.WorkflowStep;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class WorkflowStepInstance {

    @Id
    private String uid;

    private String stepName;

    private String workflowStepId;

    private String workflowInstanceId;

    private String description;

    private boolean retriable;

    private TaskStatus taskStatus;

    private String nextStepId;

    private String previousStepId;

    private WorkFlowStepType workFlowStepType;

    private String taskId;

    private Map<String, String> decisionCases = new LinkedHashMap<>();

    private Integer retryCount;

    private Integer loopCount;

    private String errorMessage;

    private LocalDateTime scheduledTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime updateTime;

    @JsonIgnore
    private Map<String, Object> inputData = new HashMap<>();

    @JsonIgnore
    private Map<String, Object> outputData = new HashMap<>();

    public WorkflowStepInstance(WorkflowStep step, String workflowInstanceId) {
        this.uid = UUID.randomUUID().toString();
        this.taskStatus = step.getStatus();
        this.description = step.getDescription();
        this.taskId = step.getTaskId();
        this.retriable = step.isRetriable();
        this.workflowInstanceId = workflowInstanceId;
        this.workflowStepId = step.getUid();
        this.stepName = step.getStepName();
        this.workFlowStepType = step.getWorkFlowStepType();
        this.retryCount = step.getRetryCount();
        this.errorMessage = step.getErrorMessage();

    }
}
