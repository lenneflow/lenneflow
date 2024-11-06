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

    @GetMapping("/api/workflows/{uid}")
    Workflow getWorkflowById(@PathVariable("uid") String uid);

    @GetMapping("/api/workflows/name/{name}")
    Workflow getWorkflowByName(@PathVariable("name") String name);

    @GetMapping("/api/workflows/steps/{uid}")
    WorkflowStep getWorkflowStepById(@PathVariable("uid") String uid);

    @GetMapping("/api/workflows/steps/name/{step-name}/workflow-uid/{workflow-uid}")
    WorkflowStep getWorkflowStepByNameAndWorkflowId(@PathVariable("step-name") String stepName, @PathVariable("workflow-uid") String workflowId);

    @GetMapping("/api/workflows/steps/workflow-uid/{workflow-uid}")
    List<WorkflowStep> getStepListByWorkflowId(@PathVariable("workflow-uid") String workflowUid);

    @GetMapping("/api/workflows/steps/workflow-name/{workflow-name}")
    List<WorkflowStep> getStepListByWorkflowName(@PathVariable("workflow-name") String workflowName);
}
