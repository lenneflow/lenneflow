package de.lenneflow.orchestrationservice.feignmodels;

import de.lenneflow.orchestrationservice.enums.DeploymentState;
import de.lenneflow.orchestrationservice.enums.PackageRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

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

    private int startDelayInSeconds;

    private boolean lazyDeployment;

    private int servicePort;

    private int assignedHostPort;

    private String serviceUrl;

    private JsonSchema inputSchema;

    private JsonSchema outputSchema;

}
