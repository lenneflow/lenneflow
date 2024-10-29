package de.lenneflow.functionservice.model;

import de.lenneflow.functionservice.enums.JsonSchemaVersion;
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
@Document
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
