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

    private String uid;

    private String name;

    private String description;

    private String type;

    private PackageRepository packageRepository;

    private DeploymentState deploymentState = DeploymentState.UNDEPLOYED;

    private String resourcePath;

    private String imageName;

    private String cpuRequest;

    private String memoryRequest;

    private boolean lazyDeployment;

    private int servicePort;

    private int assignedHostPort;

    private String serviceUrl;

    private JsonSchema inputSchema;

    private JsonSchema outputSchema;

    //private Map<String, Object> inputData = new HashMap<>();

    //private Map<String, Object> outputData = new HashMap<>();
}
