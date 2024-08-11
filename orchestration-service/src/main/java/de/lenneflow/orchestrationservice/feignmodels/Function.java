package de.lenneflow.orchestrationservice.feignmodels;

import de.lenneflow.orchestrationservice.enums.FunctionStatus;
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
public class Function implements Serializable {

    public static final String METADATA_KEY_EXECUTION_ID = "executionId";
    public static final String METADATA_KEY_STEP_INSTANCE_ID = "stepInstanceId";
    public static final String METADATA_KEY_WORKFlOW_INSTANCE_ID = "workflowInstanceId";

    private Map<String, String> metaData = new HashMap<>();

    private String functionName;

    private String functionDescription;

    private FunctionStatus  functionStatus;

    private String functionType;

    private String repositoryUrl;

    private String imageName;

    private String endPointRoot;

    private String endPointPath;

    private String errorMessage;

    private int functionPriority;

    private long creationTime;

    private long updateTime;

    private Map<String, Object> inputData = new HashMap<>();

    private Map<String, Object> outputData = new HashMap<>();
}
