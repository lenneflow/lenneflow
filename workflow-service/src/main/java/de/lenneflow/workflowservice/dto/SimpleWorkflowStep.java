package de.lenneflow.workflowservice.dto;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleWorkflowStep {

    @Hidden
    private String uid;

    private String stepName;

    private String workflowUid;

    private String description;

    private int executionOrder;

    private Integer retryCount = 0;

    private String functionId;

    private Map<String, Object> inputData = new LinkedHashMap<>();

    @Hidden
    private LocalDateTime creationTime;

    @Hidden
    private LocalDateTime updateTime;
}
