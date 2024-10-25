package de.lenneflow.orchestrationservice.controller;

import de.lenneflow.orchestrationservice.feignclients.FunctionServiceClient;
import de.lenneflow.orchestrationservice.feignclients.WorkflowServiceClient;
import de.lenneflow.orchestrationservice.model.WorkflowExecution;
import de.lenneflow.orchestrationservice.repository.WorkflowExecutionRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import de.lenneflow.orchestrationservice.helpercomponents.WorkflowRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Rest controller for the orchestration service
 *
 * @author Idrissa Ganemtore
 */
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

    @GetMapping("/workflows/{workflow-id}/start")
    public WorkflowExecution startWorkflowGet(@PathVariable(name = "workflow-id") String workflowId) {
        return workflowRunner.startWorkflow(workflowId, null);
    }

    @PostMapping("/workflows/{workflow-id}/start")
    public WorkflowExecution startWorkflowPost(@PathVariable(name = "workflow-id") String workflowId, @RequestBody Map<String, Object> inputParameters) {
        if (inputDataValid(workflowId, inputParameters)) {
            return workflowRunner.startWorkflow(workflowId, inputParameters);
        }
        return null;

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
        return workflowRunner.getCurrentExecutionState(executionId);
    }

    @GetMapping("/workflows/executions")
    public List<WorkflowExecution> executionList() {
        return workflowExecutionRepository.findAll();
    }

    private boolean inputDataValid(String workflowId, Map<String, Object> inputParameters) {
        //TODO validate with schema
        return true;
    }

}
