package de.lenneflow.orchestrationservice.feignmodels;

import de.lenneflow.orchestrationservice.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Task implements Serializable {

    public static final String METADATA_KEY_EXECUTION_ID = "executionId";
    public static final String METADATA_KEY_STEP_INSTANCE_ID = "stepInstanceId";
    public static final String METADATA_KEY_WORKFlOW_INSTANCE_ID = "workflowInstanceId";

    private Map<String, String> metaData = new HashMap<>();

    private String taskName;

    private String taskDescription;

    private TaskStatus  taskStatus;

    private String taskType;

    private String errorMessage;

    private int taskPriority;

    private int loopCount;

    private String switchCase;

    private boolean doWhileStop;

    private long creationTime;

    private long updateTime;

    private Map<String, Object> inputData = new HashMap<>();

    private Map<String, Object> outputData = new HashMap<>();
}
