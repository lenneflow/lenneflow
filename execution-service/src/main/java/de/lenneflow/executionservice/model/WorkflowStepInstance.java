package de.lenneflow.executionservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.lenneflow.executionservice.enums.TaskStatus;
import de.lenneflow.executionservice.enums.WorkFlowStepType;
import de.lenneflow.executionservice.feignmodels.WorkflowStep;
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

    private String workflowStepId;

    private String workflowInstanceId;

    private String description;

    private boolean start;

    private boolean end;

    private TaskStatus taskStatus;

    private String nextStepId;

    private String previousStepId;

    private WorkFlowStepType workFlowStepType;

    private String taskId;

    private Map<String, String> decisionCases = new LinkedHashMap<>();

    private Integer retryCount;

    private Integer loopCount;

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
        this.end = step.isEnd();
        this.start = step.isStart();
        this.taskStatus = step.getStatus();
        this.description = step.getDescription();
        this.taskId = step.getTaskId();
        this.workflowInstanceId = workflowInstanceId;
        this.workflowStepId = step.getUid();
        this.workFlowStepType = step.getWorkFlowStepType();
        this.retryCount = step.getRetryCount();

    }
}
