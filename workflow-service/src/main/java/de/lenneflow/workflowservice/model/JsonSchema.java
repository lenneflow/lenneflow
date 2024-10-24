package de.lenneflow.workflowservice.model;

import de.lenneflow.workflowservice.enums.JsonSchemaVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JsonSchema {

    @Id
    private String uid;

    private String name;

    private String description;

    private String schema;

    private JsonSchemaVersion schemaVersion;

    private LocalDateTime created;

    private LocalDateTime updated;

}
