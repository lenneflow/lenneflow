package de.lenneflow.orchestrationservice.feignclients;

import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.feignmodels.WorkflowStep;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "workflow-service")
public interface WorkflowServiceClient {

    @GetMapping("/api/workflow/get/{uuid}")
    public Workflow getWorkflow(@PathVariable("uuid") String uuid);

    @GetMapping("/api/workflow/get-step/{stepId}")
    public WorkflowStep getWorkflowStep(@PathVariable("stepId") String stepId);

    @GetMapping("/api/workflow/get-workflow-steps/{workflowId}")
    public List<WorkflowStep> getWorkflowSteps(@PathVariable("workflowId") String workflowId);

}
