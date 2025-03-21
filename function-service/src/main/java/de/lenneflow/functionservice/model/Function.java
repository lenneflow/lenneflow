package de.lenneflow.functionservice.model;


import de.lenneflow.functionservice.enums.DeploymentState;
import de.lenneflow.functionservice.enums.PackageRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;

/**
 * Function Entity.
 * @author Idrissa Ganemtore
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Function {

    @Id
    private String uid;

    @Indexed(unique = true)
    private String name;

    private String description;

    private String type;

    private PackageRepository packageRepository;

    private DeploymentState deploymentState = DeploymentState.UNDEPLOYED;

    private String resourcePath;

    private String cpuRequest;

    private String memoryRequest;

    private int startDelayInSeconds;

    private String imageName;

    private boolean lazyDeployment;

    private int servicePort;

    private int assignedHostPort;

    private String serviceUrl;

    @DocumentReference
    private JsonSchema inputSchema;

    @DocumentReference
    private JsonSchema outputSchema;

    private LocalDateTime created;

    private LocalDateTime updated;



}

