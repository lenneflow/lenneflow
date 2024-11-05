package de.lenneflow.orchestrationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(name = "Input data name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(name = "Description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(name = "Payload", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> inputData = new LinkedHashMap<>();
}
