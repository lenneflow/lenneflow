package de.lenneflow.executionservice.feignmodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.lenneflow.executionservice.enums.RunNode;
import de.lenneflow.executionservice.enums.TaskStatus;
import de.lenneflow.executionservice.enums.WorkFlowStepType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowStep {

    private String uid;

    private String workflowId;

    private String description;

    private WorkFlowStepType workFlowStepType;

    private boolean start;

    private boolean end;

    private TaskStatus status;

    private String nextStepId;

    private String previousStepId;

    private RunNode runNode;

    private String taskId;

    private Map<String, String> decisionCases = new LinkedHashMap<>();

    private Integer retryCount;

    private LocalDateTime creationTime;

    private LocalDateTime updateTime;
}
