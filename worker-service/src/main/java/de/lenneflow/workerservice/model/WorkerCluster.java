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
public class WorkerCluster {

    @Id
    private String uuid;

    @Indexed(unique = true)
    private String name;

    private String description;

    private List<WorkerNode> workerNodes;

    private String ipAddress;

    private String hostName;
}
