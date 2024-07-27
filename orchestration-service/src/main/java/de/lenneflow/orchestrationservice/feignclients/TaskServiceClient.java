package de.lenneflow.orchestrationservice.feignclients;

import de.lenneflow.orchestrationservice.feignmodels.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "task-service")
public interface TaskServiceClient {

    @GetMapping("/task/get/{uuid}")
    public Task getTask(@PathVariable String uuid);
}
