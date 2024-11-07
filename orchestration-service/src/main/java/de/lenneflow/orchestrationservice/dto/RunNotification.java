package de.lenneflow.orchestrationservice.dto;

import de.lenneflow.orchestrationservice.enums.RunStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RunNotification {

    private boolean isStepUpdate;

    private String workflowInstanceUid;

    private String workflowStepInstanceUid;

    private RunStatus status;
}
