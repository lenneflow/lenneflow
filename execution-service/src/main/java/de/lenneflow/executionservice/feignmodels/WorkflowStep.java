package de.lenneflow.executionservice.feignmodels;

import de.lenneflow.executionservice.enums.WorkFlowStepType;
import de.lenneflow.executionservice.enums.WorkflowStepStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private String uuid;

    private String workflowUuid;

    private String description;

    private boolean start;

    private boolean end;

    private WorkflowStepStatus status;

    private WorkflowStep nextStep;

    private WorkflowStep previousStep;

    private WorkFlowStepType stepType;

    private String workerTaskId;

    private  String systemTaskId;

    private Map<String, List<WorkflowStep>> decisionCases = new LinkedHashMap<>();

    private Integer retryCount;

    private long scheduledTime;

    private long startTime;

    private long endTime;

    private long updateTime;
}
