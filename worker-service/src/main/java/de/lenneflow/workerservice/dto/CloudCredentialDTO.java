package de.lenneflow.workerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CloudCredentialDTO {

    private String name;

    private String description;

    private String accountId;

    private String accessKey;

    private String secretKey;
}
