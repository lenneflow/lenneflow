package de.lenneflow.executionservice.feignclients;

import de.lenneflow.executionservice.feignmodels.Workflow;
import de.lenneflow.executionservice.feignmodels.WorkflowStep;
import de.lenneflow.executionservice.model.WorkflowInstance;
import de.lenneflow.executionservice.model.WorkflowStepInstance;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "workflow-service")
public interface WorkflowServiceClient {

    //@GetMapping("/workflow/get/{uuid}")
    default Workflow getWorkflow(@PathVariable("uuid") String uuid){
        return new WorkflowServiceClientImpl().getWorkflow(uuid);
    }

    //@GetMapping("/workflow-step/get/{workflowId}/{stepId}")
    default WorkflowStep getWorkflowStep(@PathVariable("workflowId") String workflowId, @PathVariable("stepId") String stepId){
        return new WorkflowServiceClientImpl().getWorkflowStep(workflowId, stepId);
    }

    //@GetMapping("/workflow-step/get/{workflowId}")
    default List<WorkflowStep> getWorkflowSteps(@PathVariable("workflowId") String workflowId){
        return new WorkflowServiceClientImpl().getWorkflowSteps(workflowId);
    }

}
