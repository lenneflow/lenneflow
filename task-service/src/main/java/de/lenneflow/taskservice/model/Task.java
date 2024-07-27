package de.lenneflow.taskservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.lenneflow.taskservice.enums.TaskStatus;
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
public class Task {

    @Id
    private String taskID;

    @Indexed(unique = true)
    private String taskName;

    private String taskDescription;

    private TaskStatus  taskStatus;

    private String taskType;

    private String repositoryUrl;

    private String imageName;

    private String errorMessage;

    private int taskPriority;

    private long creationTime;

    private long updateTime;

    @JsonIgnore
    private Map<String, Object> inputData = new HashMap<>();

    @JsonIgnore
    private Map<String, Object> outputData = new HashMap<>();
}
