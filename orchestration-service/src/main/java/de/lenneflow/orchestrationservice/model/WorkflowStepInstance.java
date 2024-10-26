package de.lenneflow.orchestrationservice.model;

import de.lenneflow.orchestrationservice.enums.ControlStructure;
import de.lenneflow.orchestrationservice.enums.RunStatus;
import de.lenneflow.orchestrationservice.enums.RunOrderLabel;
import de.lenneflow.orchestrationservice.feignmodels.DecisionCase;
import de.lenneflow.orchestrationservice.feignmodels.WorkflowStep;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;

/**
 * DB entity for workflow step instance
 *
 * @author Idrissa Ganemtore
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class WorkflowStepInstance {

    @Id
    private String uid;

    private String name;

    private String workflowUid;

    private String workflowInstanceUid;

    private String workflowName;

    private String description;

    private ControlStructure controlStructure;

    private int executionOrder;

    private RunStatus runStatus;

    private String functionId;

    private String subWorkflowId;

    private List<DecisionCase> decisionCases = new ArrayList<>();

    private String switchCase;

    private String stopCondition;

    private String nextStepId;

    private String previousStepId;

    private RunOrderLabel runOrderLabel;

    private Integer retryCount = 0;

    private Integer runCount = 0;

    private LocalDateTime created;

    private LocalDateTime updated;

    private String failureReason;

    private LocalDateTime scheduledTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime updateTime;

    private Map<String, Object> inputData = new LinkedHashMap<>();

    private Map<String, Object> outputData = new LinkedHashMap<>();


    public WorkflowStepInstance(WorkflowStep step, String workflowInstanceUid) {
        this.uid = UUID.randomUUID().toString();
        this.description = step.getDescription();
        this.name = step.getName();
        this.inputData = step.getInputData();
        this.functionId = step.getFunctionId();
        this.runStatus = RunStatus.NEW;
        this.workflowInstanceUid = workflowInstanceUid;
        this.controlStructure = step.getControlStructure();
        this.retryCount = step.getRetryCount();
        this.failureReason = "";
    }
}
