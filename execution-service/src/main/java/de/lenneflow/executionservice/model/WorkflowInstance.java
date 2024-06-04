package de.lenneflow.executionservice.model;


import de.lenneflow.executionservice.enums.WorkflowStatus;
import de.lenneflow.executionservice.feignmodels.Workflow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class WorkflowInstance {

    @Id
    private String uid;

    private String workflowId;

    private String workflowName;

    private String description;

    private WorkflowStatus status;

    private int version = 1;

    private List<String> stepInstanceIds = new LinkedList<>();

    private boolean statusListenerEnabled = false;

    private String ownerEmail;

    private boolean restartable = true;

    private long timeOutInSeconds;

    public WorkflowInstance(Workflow workflow) {
        this.uid = UUID.randomUUID().toString();
        this.description = workflow.getDescription();
        this.workflowId = workflow.getUid();
        this.workflowName = workflow.getName();
        this.status = workflow.getStatus();
        this.version = workflow.getVersion();
        this.ownerEmail = workflow.getOwnerEmail();
        this.restartable = workflow.isRestartable();
        this.timeOutInSeconds = workflow.getTimeOutInSeconds();

    }

}
