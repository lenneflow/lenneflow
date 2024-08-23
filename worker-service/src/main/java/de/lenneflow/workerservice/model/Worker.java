package de.lenneflow.workerservice.model;

import de.lenneflow.workerservice.enums.WorkerStatus;
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
@Document
public class Worker {

    @Id
    private String uid;

    @Indexed(unique = true)
    private String name;

    private String description;

    private String ipAddress;

    private String hostName;

    private WorkerStatus status;

    private List<String> supportedFunctionTypes = new ArrayList<>();

    private List<Integer> usedHostPorts = new ArrayList<>();

    private String currentIngress = "";

    private String kubernetesServiceUser;

    private int kubernetesApiPort;

    private String kubernetesBearerToken;

    private LocalDateTime created;

    private LocalDateTime updated;
}
