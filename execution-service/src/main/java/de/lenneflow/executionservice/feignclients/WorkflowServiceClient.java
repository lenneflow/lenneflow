package de.lenneflow.executionservice.feignclients;

import de.lenneflow.executionservice.feignmodels.Workflow;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "workflow-service")
public interface WorkflowServiceClient {

    @GetMapping("/get/{uuid}")
    public Workflow getWorkflow(@PathVariable("uuid") String uuid);

    @GetMapping("/update")
    public Workflow updateWorkflow(Workflow workflow);
}
