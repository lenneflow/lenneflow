package de.lenneflow.orchestrationservice.feignmodels;

import de.lenneflow.orchestrationservice.enums.ControlStructure;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowStep {

    private String uid;

    private String name;

    private String workflowUid;

    private String workflowName;

    private String description;

    private ControlStructure controlStructure;

    private int executionOrder;

    private String functionId;

    private String subWorkflowId;

    private List<DecisionCase> decisionCases = new ArrayList<>();

    private String switchCase;

    private String stopCondition;

    private JsonSchema inputDataSchema;

    private JsonSchema outputDataSchema;

    private Map<String, Object> inputData = new LinkedHashMap<>();

    private Integer retryCount;

    private LocalDateTime created;

    private LocalDateTime updated;
}
