package de.lenneflow.workerservice.feignmodel;


import de.lenneflow.workerservice.enums.DeploymentState;
import de.lenneflow.workerservice.enums.PackageRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Function {

    private String uid;

    private String name;

    private String description;

    private String type;

    private PackageRepository packageRepository;

    private DeploymentState deploymentState;

    private String resourcePath;

    private String imageName;

    private int servingPort;

    private int assignedHostPort;

    private String serviceUrl;

    private LocalDateTime creationTime;

    private LocalDateTime updateTime;

    private String inputSchema;

}

