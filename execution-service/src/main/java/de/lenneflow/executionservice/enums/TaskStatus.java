package de.lenneflow.executionservice.enums;

import lombok.Getter;

import java.io.Serializable;

@Getter
public enum TaskStatus implements Serializable {
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
