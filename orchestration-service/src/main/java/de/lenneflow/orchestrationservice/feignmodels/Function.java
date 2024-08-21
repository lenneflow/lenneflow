package de.lenneflow.orchestrationservice.feignmodels;

import de.lenneflow.orchestrationservice.enums.PackageRepository;
import de.lenneflow.orchestrationservice.enums.RunStatus;
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

    private String executionId;

    private String stepInstanceId;

    private String workflowInstanceId;

    private RunStatus runStatus;

    private String name;

    private String description;

    private String type;

    private PackageRepository packageRepository;

    private String deploymentFilePath;

    private String imageName;

    private String inputSchema;

    private String endPointRoot;

    private String endPointPath;

    private long creationTime;

    private long updateTime;

    private Map<String, Object> inputData = new HashMap<>();

    private Map<String, Object> outputData = new HashMap<>();
}
