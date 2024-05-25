package de.lenneflow.executionservice.feignclients;

import de.lenneflow.executionservice.feignmodels.SystemTask;
import de.lenneflow.executionservice.feignmodels.WorkerTask;
import de.lenneflow.executionservice.feignmodels.Workflow;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "task-service")
public interface TaskServiceClient {

    @GetMapping("/get/{uuid}")
    public Workflow enqueueWorkerTask(String runID, String workerTaskId);

    @GetMapping("/update")
    public Workflow enqueueSystemTask(String runID, String systemTaskId);
}
