package de.lenneflow.orchestrationservice.feignmodels;


import de.lenneflow.orchestrationservice.enums.JsonSchemaVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JsonSchema implements Serializable {

    @Id
    private String uid;

    private String name;

    private String description;

    private String schema;

    private JsonSchemaVersion schemaVersion;

    private LocalDateTime created;

    private LocalDateTime updated;

}
