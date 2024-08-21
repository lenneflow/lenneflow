package de.lenneflow.workflowservice.model;

import de.lenneflow.workflowservice.enums.ControlStructure;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
@CompoundIndex(def = "{'workflowUid': 1, 'name': 1}", unique = true)
public class WorkflowStep {

    @Id
    private String uid;

    private String name;

    private String workflowUid;

    private String workflowName;

    private String description;

    private ControlStructure controlStructure;

    private int executionOrder;

    private String functionId;

    private String subWorkflowId;

    private Map<String, WorkflowStep> decisionCases = new LinkedHashMap<>();

    private String switchCondition;

    private String stopCondition;

    private Map<String, Object> inputData = new LinkedHashMap<>();

    private Integer retryCount;

    private LocalDateTime created;

    private LocalDateTime updated;
}
