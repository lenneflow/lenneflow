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
public class RunStateDto {

    private String workflowInstanceUid;

    private String workflowRunUid;

    private RunStatus status;

    private boolean isTerminated;
}
