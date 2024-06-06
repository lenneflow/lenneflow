package de.lenneflow.executionservice.feignmodels;

import de.lenneflow.executionservice.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskResult implements Serializable {

    private Map<String, String> metaData = new HashMap<>();

    private String taskRunner;

    private TaskStatus taskStatus;

    private String failMessage;

    private Map<String, Object> outputData = new HashMap<>();

    private String outputMessage;

}
