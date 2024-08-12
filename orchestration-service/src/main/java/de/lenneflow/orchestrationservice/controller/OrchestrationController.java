package de.lenneflow.orchestrationservice.controller;

import de.lenneflow.orchestrationservice.feignclients.FunctionServiceClient;
import de.lenneflow.orchestrationservice.feignclients.WorkflowServiceClient;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.model.WorkflowExecution;
import de.lenneflow.orchestrationservice.repository.WorkflowExecutionRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import de.lenneflow.orchestrationservice.component.WorkflowRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orchestration")
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

    @GetMapping(value={"", "/"})
    public String checkService() {
        return "Welcome to the Orchestration Service! Everything is working fine!";
    }

    @GetMapping(value={"/feign"})
    public String checkFeign() {
        return functionServiceClient.getFunctionHome();
    }
    @GetMapping(value={"/feign2"})
    public String checkFeign2() {
        return workflowServiceClient.getFunctionHome();
    }

    @GetMapping("/workflow/start/name/{workflowName}")
    public ResponseEntity<WorkflowExecution>  startWorkflowGet(@PathVariable String workflowName) {
        return new ResponseEntity<>(workflowRunner.start(workflowName, null), HttpStatus.OK);
    }

    @PostMapping("/workflow/start/name/{workflowName}")
    public ResponseEntity<WorkflowExecution> startWorkflowPost(@PathVariable String workflowName, @RequestBody Map<String, Object> inputParameters) {
        if(inputParametersValid(workflowName, inputParameters)){
            return new ResponseEntity<>(workflowRunner.start(workflowName, inputParameters), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/workflow/stop/run-id/{executionId}")
    public WorkflowExecution stopWorkflow(@PathVariable String executionId) {
        return workflowRunner.stop(executionId);
    }

    @GetMapping("/workflow/pause/run-id/{executionId}")
    @ResponseStatus(HttpStatus.OK)
    public WorkflowExecution pauseWorkflow(@PathVariable String executionId) {
        return workflowRunner.pause(executionId);
    }

    @GetMapping("/workflow/resume/run-id/{executionId}")
    public WorkflowExecution resumeWorkflow(@PathVariable String executionId) {
        return workflowRunner.resume(executionId);
    }

    @GetMapping("/workflow/state/run-id/{executionId}")
    public WorkflowExecution workflowRunState(@PathVariable String executionId) {
        return workflowRunner.executionState(executionId);
    }

    private boolean inputParametersValid(String workflowId, Map<String, Object> inputParameters) {
        return true;
    }

}
