package de.lenneflow.orchestrationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class GlobalInputDataDto {

    private String name;

    private String description;

    private Map<String, Object> inputData = new LinkedHashMap<>();
}
