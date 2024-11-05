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
@Schema(name = "JsonSchema")
public class JsonSchemaDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String schema;

    @Schema(enumAsRef = true, requiredMode = Schema.RequiredMode.REQUIRED)
    private JsonSchemaVersion schemaVersion;

}