package de.lenneflow.functionservice.model;

import de.lenneflow.functionservice.enums.FunctionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Function {

    @Id
    private String functionID;

    @Indexed(unique = true)
    private String functionName;

    private String functionDescription;

    private FunctionStatus functionStatus;

    private String functionType;

    private String repositoryUrl;

    private String imageName;

    private long creationTime;

    private long updateTime;

    private Map<String, String> inputDataSchema = new HashMap<>();

}

