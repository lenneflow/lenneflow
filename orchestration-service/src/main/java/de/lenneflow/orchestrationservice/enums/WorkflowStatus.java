package de.lenneflow.orchestrationservice.enums;

import lombok.Getter;

@Getter
public enum WorkflowStatus {
    NOT_RUN,
    RUNNING,
    COMPLETED,
    FAILED,
    TIMED_OUT,
    COMPLETED_WITH_ERRORS,
    PAUSED,
    STOPPED;


}
