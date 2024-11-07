package de.lenneflow.orchestrationservice.dto;

import de.lenneflow.orchestrationservice.enums.RunStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for the function entity
 *
 * @author Idrissa Ganemtore
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QueueElement {

    private String stepInstanceId;

    private String workflowInstanceId;

    private RunStatus runStatus;

    private String serviceUrl;

    private String cpuRequest;

    private String memoryRequest;

    private String callBackUrl;

    private String failureReason;

    private String functionName;

    private String functionType;

    private Map<String, Object> inputData = new HashMap<>();

    private Map<String, Object> outputData = new HashMap<>();
}
