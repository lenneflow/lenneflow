package de.lenneflow.orchestrationservice.feignclients;

import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.feignmodels.WorkflowStep;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * This is the feign client class for the workflow service. This class acts as the interface between the orchestration service and the
 * workflow service. It will make rest calls to the workflow service and return the responses.
 *
 * @author Idrissa Ganemtore
 */
@FeignClient(name = "workflow-service")
public interface WorkflowServiceClient {

    @GetMapping("/api/workflows/{id}")
    Workflow getWorkflowById(@PathVariable("id") String id);

    @GetMapping("/api/workflows/workflow-name/{name}")
    Workflow getWorkflowByName(@PathVariable("name") String name);

    @GetMapping("/api/workflows/steps/step-id/{id}")
    WorkflowStep getWorkflowStepById(@PathVariable("id") String stepId);

    @GetMapping("/api/workflows/steps/step-name/{step-name}/workflow-id/{workflow-id}")
    WorkflowStep getWorkflowStepByNameAndWorkflowId(@PathVariable("step-name") String stepName, @PathVariable("workflow-id") String workflowId);

    @GetMapping("/api/workflows/steps/workflow-id/{workflow-id}")
    List<WorkflowStep> getStepListByWorkflowId(@PathVariable("workflow-id") String workflowId);

    @GetMapping("/api/workflows/steps/workflow-name/{workflow-name}")
    List<WorkflowStep> getStepListByWorkflowName(@PathVariable("workflow-name") String workflowName);
}
