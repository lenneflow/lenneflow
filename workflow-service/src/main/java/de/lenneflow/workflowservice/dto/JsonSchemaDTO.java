package de.lenneflow.workflowservice.dto;

import de.lenneflow.workflowservice.enums.JsonSchemaVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JsonSchemaDTO {

    private String name;

    private String description;

    private String schema;

    private JsonSchemaVersion schemaVersion;
}
