package de.lenneflow.executionservice.feignmodels;

import de.lenneflow.executionservice.enums.RunNode;
import de.lenneflow.executionservice.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    public static final String METADATA_KEY_EXECUTION_ID = "executionId";
    public static final String METADATA_KEY_STEP_INSTANCE_ID = "stepInstanceId";
    public static final String METADATA_KEY_WORKFlOW_INSTANCE_ID = "workflowInstanceId";

    private Map<String, String> metaData = new HashMap<>();

    private String taskName;

    private String taskDescription;

    private TaskStatus  taskStatus;

    private String taskType;

    private RunNode runNode;

    private int taskPriority;

    private int retryCount;

    private long creationTime;

    private long updateTime;

    private Map<String, Object> inputPayload = new HashMap<>();

    private Map<String, Object> outputPayload = new HashMap<>();

    private Map<String, Object> inputData = new HashMap<>();

    private Map<String, Object> outputData = new HashMap<>();
}
