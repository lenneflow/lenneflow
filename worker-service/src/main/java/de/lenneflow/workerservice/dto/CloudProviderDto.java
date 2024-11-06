package de.lenneflow.workerservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CloudProvider")
public enum CloudProviderDto {
    AWS, AZURE, GOOGLE
}
