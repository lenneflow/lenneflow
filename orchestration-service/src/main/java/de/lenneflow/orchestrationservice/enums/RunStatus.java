package de.lenneflow.orchestrationservice.enums;

import java.io.Serializable;


public enum RunStatus implements Serializable {
    RUNNING,
    CANCELED,
    FAILED,
    PAUSED,
    STOPPED,
    FAILED_WITH_TERMINAL_ERROR,
    COMPLETED,
    COMPLETED_WITH_ERRORS,
    SCHEDULED,
    TIMED_OUT,
    NEW,
    DEPLOYING_FUNCTIONS,
    SKIPPED;
}
