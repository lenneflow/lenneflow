package de.lenneflow.orchestrationservice.model;

import de.lenneflow.orchestrationservice.enums.RunStatus;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class WorkflowExecution {

    @Id
    private String runId;

    private String workflowInstanceId;

    private String workflowName;

    private String workflowDescription;

    private RunStatus runStatus;

    @DocumentReference
    private List<WorkflowStepInstance> runSteps;

    private String workflowType;

    private int workflowVersion;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String errors;

    private String runOutput;

    public WorkflowExecution(Workflow workflow, WorkflowInstance workflowInstance){
        this.runId = UUID.randomUUID().toString();
        this.workflowInstanceId = workflowInstance.getUid();
        this.workflowName = workflow.getName();
        this.workflowDescription = workflow.getDescription();
        this.runStatus = workflowInstance.getRunStatus();
        this.runSteps = workflowInstance.getStepInstances();
        this.startTime = workflowInstance.getStartTime();
    }

}
