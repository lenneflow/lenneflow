package de.lenneflow.functionservice.dto;


import de.lenneflow.functionservice.enums.PackageRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FunctionDTO {

    private String name;

    private String description;

    private String type;

    private PackageRepository packageRepository;

    private String resourcePath;

    private int servicePort;

    private boolean lazyDeployment;

    private String imageName;

    private String inputSchema;

    private String outputSchema;

}

