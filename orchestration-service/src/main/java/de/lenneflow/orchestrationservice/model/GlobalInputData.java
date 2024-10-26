package de.lenneflow.orchestrationservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DB entity for global input data instance
 *
 * @author Idrissa Ganemtore
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class GlobalInputData {

    @Id
    private String uid;

    private String name;

    private String description;

    private Map<String, Object> inputData = new LinkedHashMap<>();
}
