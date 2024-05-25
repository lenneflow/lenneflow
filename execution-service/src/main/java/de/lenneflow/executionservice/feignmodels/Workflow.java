package de.lenneflow.executionservice.feignmodels;

import de.lenneflow.executionservice.enums.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Workflow {

    @Id
    private String uuid;

    @Indexed(unique = true)
    private String name;

    private String description;

    private WorkflowStatus status;

    private int version = 1;

    private List<WorkflowStep> tasks = new LinkedList<>();

    private boolean statusListenerEnabled = false;

    private String ownerEmail;

    private boolean restartable = true;

    private long timeOutInSeconds;

}
