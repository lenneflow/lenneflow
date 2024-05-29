package de.lenneflow.executionservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.lenneflow.executionservice.enums.TaskStatus;
import de.lenneflow.executionservice.enums.WorkFlowStepType;
import de.lenneflow.executionservice.feignmodels.Task;
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
public class WorkflowStepInstance {

    @Id
    private String instanceId;

    private String stepId;

    private String workflowId;

    private String workflowInstanceId;

    private String description;

    private boolean start;

    private boolean end;

    private TaskStatus taskStatus;

    @DocumentReference
    private WorkflowStepInstance nextStep;

    @DocumentReference
    private WorkflowStepInstance previousStep;

    private WorkFlowStepType stepType;

    @DocumentReference
    private Task task;

    @DocumentReference
    private Map<String, List<WorkflowStepInstance>> decisionCases = new LinkedHashMap<>();

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
