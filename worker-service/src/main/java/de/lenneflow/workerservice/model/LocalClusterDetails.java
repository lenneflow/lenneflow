package de.lenneflow.workerservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class LocalClusterDetails {

    @Id
    private String uid;

    private String name;

    private String description;

    private String ingressServiceName;

    private String serviceUser;

    private String ipAddress;

    private int apiServerPort;

    private String sessionToken;

    private LocalDateTime created;

    private LocalDateTime updated;
}
