package de.lenneflow.workflowservice.enums;

import lombok.Getter;

@Getter
public enum WorkflowStatus {

    RUNNING(false, false),
    COMPLETED(true, true),
    FAILED(true, false),
    TIMED_OUT(true, false),
    TERMINATED(true, false),
    PAUSED(false, true),
    STOPPED(true, false);

    private final boolean terminal;

    private final boolean successful;

    WorkflowStatus(boolean terminal, boolean successful) {
        this.terminal = terminal;
        this.successful = successful;
    }

}
