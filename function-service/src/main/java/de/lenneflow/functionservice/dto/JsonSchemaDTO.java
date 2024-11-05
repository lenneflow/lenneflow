package de.lenneflow.functionservice.dto;

import de.lenneflow.functionservice.enums.JsonSchemaVersion;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class JsonSchemaDTO {

    @Schema(name = "Schema name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(name = "Description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(name = "Schema", requiredMode = Schema.RequiredMode.REQUIRED)
    private String schema;

    @Schema(name = "Schema version", examples = {"V4","V6","V7","V201909", "V202012"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private JsonSchemaVersion schemaVersion;

}
