package de.lenneflow.orchestrationservice.feignclients;

import de.lenneflow.orchestrationservice.feignmodels.Task;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "task-service")
public interface TaskServiceClient {

    //public Task getTask(String taskId);

    default Task getTask(String taskId){
        return new TaskServiceClientImpl().getTask(taskId);
    }

}
