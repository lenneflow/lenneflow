package de.lenneflow.executionservice.feignclients;

import de.lenneflow.executionservice.feignmodels.Workflow;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "task-service")
public interface TaskServiceClient {

    @GetMapping("/get/{uuid}")
    public Workflow enqueueTask(String runId, String taskId);

}
