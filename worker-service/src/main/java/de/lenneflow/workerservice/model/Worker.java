package de.lenneflow.workerservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Worker {

    @Id
    private String uuid;

    @Indexed(unique = true)
    private String name;

    private String description;

    private String nameSpace;

    private List<String> taskTypes;

    private String repositoryUrl;

    private String imageName;
}
