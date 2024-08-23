package de.lenneflow.orchestrationservice.controller;

import de.lenneflow.orchestrationservice.feignclients.FunctionServiceClient;
import de.lenneflow.orchestrationservice.feignclients.WorkflowServiceClient;
import de.lenneflow.orchestrationservice.model.WorkflowExecution;
import de.lenneflow.orchestrationservice.repository.WorkflowExecutionRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import de.lenneflow.orchestrationservice.component.WorkflowRunner;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/control")
public class OrchestrationController {

    final WorkflowExecutionRepository workflowExecutionRepository;
    final WorkflowServiceClient workflowServiceClient;
    final FunctionServiceClient functionServiceClient;
    final WorkflowInstanceRepository workflowInstanceRepository;
    final WorkflowStepInstanceRepository workflowStepInstanceRepository;
    final WorkflowRunner workflowRunner;

    public OrchestrationController(WorkflowExecutionRepository workflowExecutionRepository, WorkflowServiceClient workflowServiceClient, FunctionServiceClient functionServiceClient, WorkflowInstanceRepository workflowInstanceRepository, WorkflowStepInstanceRepository workflowStepInstanceRepository, WorkflowRunner workflowRunner) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowServiceClient = workflowServiceClient;
        this.functionServiceClient = functionServiceClient;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
        this.workflowRunner = workflowRunner;
    }

    @Hidden
    @GetMapping(value={"/check"})
    public String checkService() {
        return "Welcome to the Orchestration Service!";
    }

    @Hidden
    @GetMapping(value={"/feign"})
    public String checkFeign() {
        return functionServiceClient.getFunctionHome();
    }


    @GetMapping("/workflows/{workflow-id}/start")
    public ResponseEntity<WorkflowExecution>  startWorkflowGet(@PathVariable(name = "workflow-id") String workflowId) {
        return new ResponseEntity<>(workflowRunner.startWorkflow(workflowId, null), HttpStatus.OK);
    }

    @PostMapping("/workflows/{workflow-id}/start")
    public ResponseEntity<WorkflowExecution> startWorkflowPost(@PathVariable(name = "workflow-id") String workflowId, @RequestBody Map<String, Object> inputParameters) {
        if(inputParametersValid(workflowId, inputParameters)){
            return new ResponseEntity<>(workflowRunner.startWorkflow(workflowId, inputParameters), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/workflows/executions/{execution-id}/stop")
    public WorkflowExecution stopWorkflow(@PathVariable(name = "execution-id") String executionId) {
        return workflowRunner.stopWorkflow(executionId);
    }

    @GetMapping("/workflows/executions/{execution-id}/pause")
    @ResponseStatus(HttpStatus.OK)
    public WorkflowExecution pauseWorkflow(@PathVariable(name = "execution-id") String executionId) {
        return workflowRunner.pauseWorkflow(executionId);
    }

    @GetMapping("/workflows/executions/{execution-id}/resume")
    public WorkflowExecution resumeWorkflow(@PathVariable(name = "execution-id") String executionId) {
        return workflowRunner.resumeWorkflow(executionId);
    }

    @GetMapping("/workflows/executions/{execution-id}/state")
    public WorkflowExecution workflowRunState(@PathVariable(name = "execution-id") String executionId) {
        return workflowRunner.executionState(executionId);
    }

    private boolean inputParametersValid(String workflowId, Map<String, Object> inputParameters) {
        return true;
    }

}
