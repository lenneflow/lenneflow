package de.lenneflow.executionservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lenneflow.executionservice.feignmodels.Task;
import de.lenneflow.executionservice.feignmodels.Workflow;

import java.io.IOException;

public class Util {

    public static Task deserializeTask(byte[] serializedTask) {
        ObjectMapper mapper = new ObjectMapper();
        Task taskResult = null;
        try {
            taskResult = mapper.readValue(serializedTask, Task.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return taskResult;
    }

    public static String serializeTask(Task task) {
        ObjectMapper mapper = new ObjectMapper();
        String serializedTask = null;
        try {
            serializedTask = mapper.writeValueAsString(task);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return serializedTask;
    }


}
