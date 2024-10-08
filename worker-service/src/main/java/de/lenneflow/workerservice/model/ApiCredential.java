package de.lenneflow.workerservice.model;

import io.swagger.v3.oas.annotations.Hidden;
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
public class ApiCredential {

    @Id
    private String uid;

    private String description;

    private String apiServerEndpoint;

    private String apiAuthToken;

    private LocalDateTime expiresAt;

    private LocalDateTime created;

    private LocalDateTime updated;
}
