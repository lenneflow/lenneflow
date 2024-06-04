package de.lenneflow.executionservice.model;

import de.lenneflow.executionservice.enums.WorkflowStatus;
import de.lenneflow.executionservice.feignmodels.Workflow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class WorkflowExecution {

    @Id
    private String uid;

    private String workflowInstanceId;

    private String workflowId;

    private String workflowName;

    private String workflowDescription;

    private WorkflowStatus workflowStatus;

    @DocumentReference
    private List<WorkflowStepInstance> workflowStepInstances;

    private String workflowType;

    private int workflowVersion;

    private String runStartTime;

    private String runEndTime;

    private String runErrors;

    private String runOutput;

    public WorkflowExecution(Workflow workflow, WorkflowInstance workflowInstance, List<WorkflowStepInstance> workflowStepInstances){
        this.uid = UUID.randomUUID().toString();
        this.workflowId = workflow.getUid();
        this.workflowInstanceId = workflowInstance.getUid();
        this.workflowName = workflow.getName();
        this.workflowDescription = workflow.getDescription();
        this.workflowStatus = workflow.getStatus();
        this.workflowStepInstances = workflowStepInstances;
        this.workflowVersion = workflow.getVersion();
        this.runStartTime = LocalDateTime.now().toString();


    }

}
