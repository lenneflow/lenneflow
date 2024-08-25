package de.lenneflow.orchestrationservice.feignmodels;

import de.lenneflow.orchestrationservice.enums.DeploymentState;
import de.lenneflow.orchestrationservice.enums.PackageRepository;
import de.lenneflow.orchestrationservice.enums.RunStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

import java.time.LocalDateTime;
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

    private DeploymentState deploymentState;

    private String resourcePath;

    private int servingPort;

    private String imageName;

    private String inputSchema;

    private String serviceUrl;

    private LocalDateTime creationTime;

    private LocalDateTime updateTime;

    private Map<String, Object> inputData = new HashMap<>();

    private Map<String, Object> outputData = new HashMap<>();
}
