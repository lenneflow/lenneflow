package de.lenneflow.orchestrationservice.model;


import de.lenneflow.orchestrationservice.enums.WorkflowStatus;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

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

    private boolean errorsPresent;

    private Map<String, String> errorMessages = new HashMap<>();

    private String description;

    private WorkflowStatus status;

    private int version = 1;

    private Map<String, Object> inputParameters = new HashMap<>();

    private List<String> stepInstanceIds = new LinkedList<>();

    private boolean statusListenerEnabled = false;

    private String ownerEmail;

    private boolean restartable = true;

    private long timeOutInSeconds;

    public WorkflowInstance(Workflow workflow, Map<String, Object> inputParameters) {
        this.uid = UUID.randomUUID().toString();
        this.description = workflow.getDescription();
        this.workflowId = workflow.getUid();
        this.workflowName = workflow.getName();
        this.inputParameters = inputParameters;
        this.status = workflow.getStatus();
        this.version = workflow.getVersion();
        this.ownerEmail = workflow.getOwnerEmail();
        this.restartable = workflow.isRestartable();
        this.timeOutInSeconds = workflow.getTimeOutInSeconds();

    }

}
