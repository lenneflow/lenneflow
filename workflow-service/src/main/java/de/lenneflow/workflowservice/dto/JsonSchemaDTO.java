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
public class JsonSchemaDTO {

    @Schema(name = "Schema Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(name = "Description", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(name = "Schema", requiredMode = Schema.RequiredMode.REQUIRED)
    private String schema;

    @Schema(name = "Schema Version", enumAsRef = true, requiredMode = Schema.RequiredMode.REQUIRED)
    private JsonSchemaVersion schemaVersion;
}
