package de.lenneflow.workerservice.feignmodel;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Function {

    private String uid;

    private String name;

    private String description;

    private String type;

    private String packageRepository;

    private String imageName;

    private LocalDateTime creationTime;

    private LocalDateTime updateTime;

    private String inputSchema;

}

