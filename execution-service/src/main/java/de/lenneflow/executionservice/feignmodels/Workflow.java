package de.lenneflow.executionservice.feignmodels;

import de.lenneflow.executionservice.enums.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Workflow {

    private String id;

    private String name;

    private String description;

    private WorkflowStatus status;

    private int version = 1;

    private List<WorkflowStep> steps = new LinkedList<>();

    private boolean statusListenerEnabled = false;

    private String ownerEmail;

    private boolean restartable = true;

    private long timeOutInSeconds;

    private LocalDateTime creationTime;

    private LocalDateTime updateTime;

}
