package de.lenneflow.orchestrationservice.dto;

import de.lenneflow.orchestrationservice.enums.RunStatus;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DB entity for Workflow execution
 *
 * @author Idrissa Ganemtore
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowExecution {

    //the run UID is the workflow Instance UID
    private String runUid;

    private String workflowName;

    private String workflowDescription;

    private RunStatus runStatus;

    private List<WorkflowStepInstance> runSteps;

    private int workflowVersion;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String failureReason;

    private Map<String, Object> outputData = new HashMap<>();

    public WorkflowExecution(WorkflowInstance workflowInstance) {
        this.runUid = workflowInstance.getUid();
        this.workflowName = workflowInstance.getName();
        this.workflowDescription = workflowInstance.getDescription();
        this.runStatus = workflowInstance.getRunStatus();
        this.runSteps = workflowInstance.getStepInstances();
        this.startTime = workflowInstance.getStartTime();
        this.endTime = workflowInstance.getEndTime();
        this.failureReason = workflowInstance.getFailureReason();
        this.outputData = workflowInstance.getOutputData();
    }

}
