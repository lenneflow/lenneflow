package de.lenneflow.executionservice.model;

import de.lenneflow.executionservice.enums.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class WorkflowExecution {

    @Id
    private String id;

    @DocumentReference
    private WorkflowInstance workflowInstance;

    @DocumentReference
    private WorkflowStepInstance workflowStepInstance;

    private String workflowName;

    private String workflowDescription;

    private WorkflowStatus workflowStatus;

    private String workflowType;

    private String workflowVersion;

    private String runStartTime;

    private String runEndTime;

    private String runErrors;

    private String runOutput;

}
