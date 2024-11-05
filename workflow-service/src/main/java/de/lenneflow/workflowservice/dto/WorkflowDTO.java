package de.lenneflow.workflowservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowDTO {

    @Schema(name = "Workflow Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(name = "Workflow Description", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(name = "Timeout", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long timeOutInSeconds;

    @Schema(name = "Input Data Schema", requiredMode = Schema.RequiredMode.REQUIRED)
    private String inputDataSchemaUid;

    @Schema(name = "Output Data Schema", requiredMode = Schema.RequiredMode.REQUIRED)
    private String outputDataSchemaUid;
}
