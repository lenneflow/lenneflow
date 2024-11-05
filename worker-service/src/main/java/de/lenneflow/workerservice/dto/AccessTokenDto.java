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
@Schema(name = "AccessToken")
public class AccessTokenDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String expiration;
}
