package de.lenneflow.workflowservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowDTO {

    private String uid;

    private String name;

    private String description;

    private boolean restartable = true;

    private long timeOutInSeconds;
}
