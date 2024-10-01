package de.lenneflow.orchestrationservice.feignmodels;

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
public class DecisionCase {

    private String name;

    private String functionId;

    private String subWorkflowId;

    private Map<String, Object> inputData = new LinkedHashMap<>();

    private Integer retryCount;
}
