package de.lenneflow.workflowservice.enums;

public enum WorkFlowStepType {
    SIMPLE,
    DYNAMIC,
    FORK_JOIN,
    FORK_JOIN_DYNAMIC,
    DECISION,
    SWITCH,
    JOIN,
    DO_WHILE,
    SUB_WORKFLOW,
    START_WORKFLOW,
    EVENT,
    WAIT,
    HUMAN,
    USER_DEFINED,
    HTTP,
    LAMBDA,
    INLINE,
    EXCLUSIVE_JOIN,
    TERMINATE,
    KAFKA_PUBLISH,
    JSON_JQ_TRANSFORM,
    SET_VARIABLE,
    NOOP;
}
