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

    private int apiPort;

    private String hostName;

    private WorkerStatus status;

    private String serviceUser;

    private String bearerToken;

    private LocalDateTime created;

    private LocalDateTime updated;
}
