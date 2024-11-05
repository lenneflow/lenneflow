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
public class FunctionDTO {

    @Schema(name = "Product ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(name = "Description", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(name = "The type of function", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Schema(name = "Package repository", example = "DOCKER_HUB", requiredMode = Schema.RequiredMode.REQUIRED)
    private PackageRepository packageRepository;

    @Schema(name = "Resource path", example = "/api/function/process", requiredMode = Schema.RequiredMode.REQUIRED)
    private String resourcePath;

    @Schema(name = "The container port of the function", example = "8080", requiredMode = Schema.RequiredMode.REQUIRED)
    private int servicePort;

    @Schema(name = "Is lazy deployment", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean lazyDeployment;

    @Schema(name = "Repository image name", example = "lenneflow\\dummy-function-random", requiredMode = Schema.RequiredMode.REQUIRED)
    private String imageName;

    @Schema(name = "ID of the Input Schema", requiredMode = Schema.RequiredMode.REQUIRED)
    private String inputSchemaUid;

    @Schema(name = "ID of the Output Schema", requiredMode = Schema.RequiredMode.REQUIRED)
    private String outputSchemaUid;

}

