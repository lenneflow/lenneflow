package de.lenneflow.orchestrationservice.feignmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DecisionCase implements Serializable {

    private String name;

    private String functionUid;

    private String subWorkflowUid;

    private boolean isSubWorkflow;

    private Map<String, Object> inputData = new LinkedHashMap<>();

    private Integer retryCount;
}
