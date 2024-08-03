package de.lenneflow.functionservice.enums;

public enum FunctionStatus {
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
