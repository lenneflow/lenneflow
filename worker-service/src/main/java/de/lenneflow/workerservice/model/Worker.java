package de.lenneflow.workerservice.model;

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
@Document
public class Worker {

    @Id
    private String uuid;

    @Indexed(unique = true)
    private String name;

    private String description;

    private WorkerCluster cluster;

    private String ipAddress;

    private String hostName;
}
