package de.lenneflow.workerservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CloudCredential")
public class CloudCredentialDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String accountId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessKey;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String secretKey;
}
