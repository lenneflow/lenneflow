package de.lenneflow.orchestrationservice.feignclients;

import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.feignmodels.WorkflowStep;
import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@FeignClient(name = "workflow-service", url = "http://localhost:47005")
@FeignClient(name = "workflow-service")
public interface WorkflowServiceClient {

    @GetMapping("/api/workflows/{id}")
    public Workflow getWorkflowById(@PathVariable("id") String id);

    @GetMapping("/api/workflows")
    public Workflow getWorkflowByName(@RequestParam("name") String name);

    @GetMapping("/api/workflows/steps/{id}")
    public WorkflowStep getWorkflowStepById(@PathVariable("id") String stepId);

    @GetMapping("/api/workflows/steps")
    public WorkflowStep getWorkflowStepByName(@RequestParam("name")  String stepName);

    @GetMapping("/api/workflows/steps/workflow-id/{workflow-id}")
    public List<WorkflowStep> getStepListByWorkflowId(@PathVariable("workflow-id") String workflowId);

    @GetMapping("/api/workflows/steps")
    public List<WorkflowStep> getStepListByWorkflowName(@RequestParam("workflow-name") String workflowName);

    @GetMapping("/api/workflows/check")
    public String getFunctionHome();
}
