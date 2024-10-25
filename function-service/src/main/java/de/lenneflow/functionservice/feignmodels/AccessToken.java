package de.lenneflow.functionservice.feignmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * This is the access token data transfer object used by the feign controller to retrieve a token from the worker service.
 * @author Idrissa Ganemtore
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccessToken {

    private String uid;

    private String token;

    private String description;

    private LocalDateTime expiration;

    private LocalDateTime updated;
}
