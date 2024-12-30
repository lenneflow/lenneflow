package de.lenneflow.functionservice.dto;


import de.lenneflow.functionservice.enums.PackageRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data transfer object for the entity function
 * @author Idrissa Ganemtore
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Function")
public class FunctionDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Schema(enumAsRef = true, requiredMode = Schema.RequiredMode.REQUIRED)
    private PackageRepository packageRepository;

    @Schema(example = "/api/function/process", requiredMode = Schema.RequiredMode.REQUIRED)
    private String resourcePath;

    @Schema(example = "8080", requiredMode = Schema.RequiredMode.REQUIRED)
    private int servicePort;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean lazyDeployment;

    @Schema(example = "lenneflow\\dummy-function-random", requiredMode = Schema.RequiredMode.REQUIRED)
    private String imageName;

    @Schema(examples = {"250m", "0.5"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String cpuRequest;

    @Schema(examples = {"300Mi"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String memoryRequest;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private int startDelayInSeconds;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String inputSchemaUid;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String outputSchemaUid;

}

