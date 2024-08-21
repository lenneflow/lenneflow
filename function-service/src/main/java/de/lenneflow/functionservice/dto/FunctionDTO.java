package de.lenneflow.functionservice.dto;


import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FunctionDTO {

    @Hidden
    private String Uid;

    private String name;

    private String description;

    private String type;

    private String packageRepository;

    private String imageName;

    private String inputSchema;

}

