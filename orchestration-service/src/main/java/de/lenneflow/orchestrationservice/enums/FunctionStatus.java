package de.lenneflow.orchestrationservice.enums;

import java.io.Serializable;


public enum FunctionStatus implements Serializable {
    IN_PROGRESS,
    CANCELED,
    FAILED,
    FAILED_WITH_TERMINAL_ERROR,
    COMPLETED,
    COMPLETED_WITH_ERRORS,
    SCHEDULED,
    TIMED_OUT,
    NOT_RUN,
    SKIPPED;
}
