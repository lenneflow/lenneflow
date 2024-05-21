package de.lenneflow.executionservice.feignmodels;

import de.lenneflow.executionservice.utils.WorkFlowTaskType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowTask {

    @Id
    private String uuid;

    @Indexed(unique = true)
    private String name;

    private String description;

    private WorkFlowTaskType taskType;

    private Map<String, List<WorkflowTask>> decisionCases = new LinkedHashMap<>();

    private Integer retryCount;
}
