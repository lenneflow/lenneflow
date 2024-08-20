package de.lenneflow.workerservice.dto;

import de.lenneflow.workerservice.enums.WorkerStatus;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkerDTO {

    @Hidden
    private String uid;

    private String name;

    private String description;

    private String ipAddress;

    private int apiPort;

    private String hostName;

    private String serviceUser;

    private String bearerToken;

    @Hidden
    private WorkerStatus status;

    @Hidden
    private LocalDateTime created;

    @Hidden
    private LocalDateTime updated;

}
