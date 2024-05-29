package de.lenneflow.executionservice.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {

    IN_PROGRESS(false, true, true),
    CANCELED(true, false, false),
    FAILED(true, false, true),
    FAILED_WITH_TERMINAL_ERROR(true, false, false),
    COMPLETED(true, true, true),
    COMPLETED_WITH_ERRORS(true, true, true),
    SCHEDULED(false, true, true),
    TIMED_OUT(true, false, true),
    NOT_RUN(false, false, false),
    SKIPPED(true, true, false);

    private final boolean terminal;

    private final boolean successful;

    private final boolean retriable;

    TaskStatus(boolean terminal, boolean successful, boolean retriable) {
        this.terminal = terminal;
        this.successful = successful;
        this.retriable = retriable;
    }

}
