package de.lenneflow.executionservice.feignclients;

import de.lenneflow.executionservice.feignmodels.Task;
import de.lenneflow.executionservice.feignmodels.Workflow;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "task-service")
public interface TaskServiceClient {

    //public Task getTask(String taskId);

    default Task getTask(String taskId){
        return new TaskServiceClientImpl().getTask(taskId);
    }

}
