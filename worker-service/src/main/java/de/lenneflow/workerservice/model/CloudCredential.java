package de.lenneflow.workerservice.model;

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
public class CloudCredential {

    @Id
    private String uid;

    private String name;

    private String description;

    private String accountId;

    private String accessKey;

    private String secretKey;

    private LocalDateTime created;

    private LocalDateTime updated;
}
