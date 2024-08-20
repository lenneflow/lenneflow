package de.lenneflow.orchestrationservice.model;


import de.lenneflow.orchestrationservice.enums.RunStatus;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.feignmodels.WorkflowStep;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class WorkflowInstance {

    @Id
    private String uid;

    private String workflowUid;

    private String name;

    private RunStatus runStatus;

    private String description;

    @DocumentReference
    private List<WorkflowStepInstance> stepInstances = new LinkedList<>();

    private boolean statusListenerEnabled = false;

    private boolean restartable = true;

    private long timeOutInSeconds = Long.MAX_VALUE;

    private LocalDateTime created;

    private LocalDateTime updated;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public WorkflowInstance(Workflow workflow) {
        this.uid = UUID.randomUUID().toString();
        this.name = workflow.getName();
        this.description = workflow.getDescription();
        this.workflowUid = workflow.getUid();
        this.runStatus = RunStatus.NEW;
        this.restartable = workflow.isRestartable();
        this.timeOutInSeconds = workflow.getTimeOutInSeconds();
        this.created = LocalDateTime.now();
        this.updated = LocalDateTime.now();

    }

}
