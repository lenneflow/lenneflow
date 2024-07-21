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
    @Id
    private String stepId;

    private String workflowId;

    private String description;

    private boolean start;

    private boolean end;

    private WorkflowStepStatus status;

    private WorkflowStep nextStep;

    private WorkflowStep previousStep;

    private WorkFlowStepType stepType;

    private String taskId;

    @DocumentReference
    private Map<String, List<WorkflowStep>> decisionCases = new LinkedHashMap<>();

    private Integer retryCount;

    private long scheduledTime;

    private long startTime;

    private long endTime;

    private long updateTime;

    @JsonIgnore
    private Map<String, Object> inputPayload = new HashMap<>();

    @JsonIgnore
    private Map<String, Object> outputPayload = new HashMap<>();

    @JsonIgnore
    private Map<String, Object> inputData = new HashMap<>();

    @JsonIgnore
    private Map<String, Object> outputData = new HashMap<>();
}
