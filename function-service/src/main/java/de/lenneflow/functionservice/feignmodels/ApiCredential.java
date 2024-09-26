package de.lenneflow.functionservice.feignmodels;

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
public class ApiCredential {

    private String uid;

    private String description;

    private String apiServerEndpoint;

    private String apiAuthToken;

    private LocalDateTime expiresAt;

}
