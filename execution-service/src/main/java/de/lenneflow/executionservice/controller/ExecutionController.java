package de.lenneflow.executionservice.controller;

import de.lenneflow.executionservice.enums.WorkflowStatus;
import de.lenneflow.executionservice.feignclients.TaskServiceClient;
import de.lenneflow.executionservice.feignclients.WorkflowServiceClient;
import de.lenneflow.executionservice.feignmodels.Workflow;
import de.lenneflow.executionservice.model.WorkflowExecution;
import de.lenneflow.executionservice.model.WorkflowInstance;
import de.lenneflow.executionservice.repository.WorkflowExecutionRepository;
import de.lenneflow.executionservice.repository.WorkflowInstanceRepository;
import de.lenneflow.executionservice.repository.WorkflowStepInstanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/execution/workflow")
public class ExecutionController {

    final WorkflowExecutionRepository workflowExecutionRepository;
    final WorkflowServiceClient workflowServiceClient;
    final TaskServiceClient taskServiceClient;
    final WorkflowInstanceRepository workflowInstanceRepository;
    final WorkflowStepInstanceRepository workflowStepInstanceRepository;
    final WorkflowRunner workflowRunner;

    public ExecutionController(WorkflowExecutionRepository workflowExecutionRepository, WorkflowServiceClient workflowServiceClient, TaskServiceClient taskServiceClient, WorkflowInstanceRepository workflowInstanceRepository, WorkflowStepInstanceRepository workflowStepInstanceRepository, WorkflowRunner workflowRunner) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowServiceClient = workflowServiceClient;
        this.taskServiceClient = taskServiceClient;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
        this.workflowRunner = workflowRunner;
    }

    @GetMapping("/start/{workflowId}")
    public WorkflowExecution startWorkflow(@PathVariable String workflowId) {
       return workflowRunner.start(workflowId);
    }

    @GetMapping("/stop/{executionId}")
    public WorkflowExecution stopWorkflow(@PathVariable String executionId) {
        return workflowRunner.stop(executionId);
    }

    @GetMapping("/pause/{executionId}")
    @ResponseStatus(HttpStatus.OK)
    public WorkflowExecution pauseWorkflow(@PathVariable String executionId) {
        return workflowRunner.pause(executionId);
    }

    @GetMapping("/resume/{executionId}")
    public WorkflowExecution resumeWorkflow(@PathVariable String executionId) {
        return workflowRunner.resume(executionId);
    }

}
