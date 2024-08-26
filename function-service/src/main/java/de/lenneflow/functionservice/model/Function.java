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

import java.time.LocalDateTime;

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

    private String imageName;

    private int servingPort;

    private int assignedHostPort;

    private String serviceUrl;

    private LocalDateTime creationTime;

    private LocalDateTime updateTime;

    private String inputSchema;

}

