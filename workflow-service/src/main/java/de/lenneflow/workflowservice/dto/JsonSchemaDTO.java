package de.lenneflow.workflowservice.dto;

import de.lenneflow.workflowservice.enums.JsonSchemaVersion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "JsonSchema")
public class JsonSchemaDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String schema;

    @Schema(enumAsRef = true, requiredMode = Schema.RequiredMode.REQUIRED)
    private JsonSchemaVersion schemaVersion;
}
