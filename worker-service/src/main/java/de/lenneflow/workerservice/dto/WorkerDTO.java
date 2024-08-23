package de.lenneflow.workerservice.dto;

import de.lenneflow.workerservice.enums.WorkerStatus;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkerDTO {

    private String name;

    private String description;

    private String ipAddress;

    private int kubernetesApiPort;

    private String hostName;

    private String kubernetesServiceUser;

    private String kubernetesBearerToken;

    private List<String> supportedFunctionTypes = new ArrayList<>();

}
