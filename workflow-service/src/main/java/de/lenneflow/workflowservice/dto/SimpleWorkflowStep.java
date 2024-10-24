package de.lenneflow.workflowservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleWorkflowStep {


    private String name;

    private String workflowUid;

    private String description;

    private int executionOrder;

    private Integer retryCount = 0;

    private String functionId;

    private Map<String, Object> inputData = new LinkedHashMap<>();

}
