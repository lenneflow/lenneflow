package de.lenneflow.executionservice.model;

import de.lenneflow.executionservice.enums.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class WorkflowExecution {

    @Id
    private String executionID;

    private String workflowID;

    private String workflowName;

    private String workflowDescription;

    private WorkflowStatus workflowStatus;

    private String workflowType;

    private String workflowVersion;

    private String workflowStartTime;

    private String workflowEndTime;

    private String workflowErrors;

    private String workflowOutput;

}
