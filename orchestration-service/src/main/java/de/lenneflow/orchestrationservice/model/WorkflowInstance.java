package de.lenneflow.orchestrationservice.model;


import de.lenneflow.orchestrationservice.enums.RunStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;
import java.util.*;

/**
 * DB entity for workflow instance
 *
 * @author Idrissa Ganemtore
 */
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

    private String failureReason;

    private LocalDateTime created;

    private Map<String, Object> inputData = new LinkedHashMap<>();

    private Map<String, Object> outputData = new LinkedHashMap<>();

    private LocalDateTime updated;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

}
