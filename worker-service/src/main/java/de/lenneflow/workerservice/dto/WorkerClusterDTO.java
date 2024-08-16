package de.lenneflow.workerservice.dto;

import de.lenneflow.workerservice.model.Worker;
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
public class WorkerClusterDTO {

    @Id
    private String uuid;

    @Indexed(unique = true)
    private String name;

    private String description;

    private List<Worker> workers;

    private String ipAddress;

    private String hostName;
}
