package de.lenneflow.workflowservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Workflow {

    @Id
    private String uid;

    @Indexed(unique = true)
    private String name;

    private String description;

    @DocumentReference
    private List<WorkflowStep> steps = new LinkedList<>();

    private boolean statusListenerEnabled = false;

    private boolean restartable = true;

    private long timeOutInSeconds = Long.MAX_VALUE;

    @DocumentReference
    private JsonSchema inputDataSchema;

    @DocumentReference
    private JsonSchema outputDataSchema;

    private LocalDateTime created;

    private LocalDateTime updated;

}
