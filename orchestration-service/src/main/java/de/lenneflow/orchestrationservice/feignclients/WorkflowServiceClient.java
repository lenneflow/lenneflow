package de.lenneflow.orchestrationservice.feignclients;

import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.feignmodels.WorkflowStep;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

//@FeignClient(name = "workflow-service", url = "http://localhost:47005")
@FeignClient(name = "workflow-service")
public interface WorkflowServiceClient {

    @GetMapping("/api/workflow/get/{uuid}")
    public Workflow getWorkflow(@PathVariable("uuid") String uuid);

    @GetMapping("/api/workflow/get/name/{name}")
    public Workflow getWorkflowByName(@PathVariable("name") String uuid);

    @GetMapping("/api/workflow/step/get/id/{stepId}")
    public WorkflowStep getWorkflowStepById(@PathVariable("stepId") String stepId);

    @GetMapping("/api/workflow/step/get/name/{stepName}")
    public WorkflowStep getWorkflowStepByName(@PathVariable("stepName") String stepId);

    @GetMapping("/api/workflow/step/list/workflow/id/{workflowId}")
    public List<WorkflowStep> getStepListByWorkflowId(@PathVariable("workflowId") String workflowId);

    @GetMapping("/api/workflow/step/list/workflow/name/{workflowName}")
    public List<WorkflowStep> getStepListByWorkflowName(@PathVariable("workflowName") String workflowId);

    @GetMapping("/api/workflow")
    public String getFunctionHome();
}
