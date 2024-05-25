package de.lenneflow.executionservice.controller;

import de.lenneflow.executionservice.enums.WorkflowStatus;
import de.lenneflow.executionservice.feignclients.WorkflowServiceClient;
import de.lenneflow.executionservice.feignmodels.Workflow;
import de.lenneflow.executionservice.model.WorkflowExecution;
import de.lenneflow.executionservice.repository.WorkflowExecutionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/execution/workflow")
public class ExecutionController {

    final
    WorkflowExecutionRepository workflowExecutionRepository;

    final WorkflowServiceClient workflowServiceClient;

    public ExecutionController(WorkflowExecutionRepository workflowExecutionRepository, WorkflowServiceClient workflowServiceClient) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowServiceClient = workflowServiceClient;
    }

    @GetMapping("/start/{id}")
    public WorkflowExecution startWorkflow(@PathVariable String id) {
        WorkflowExecution workflowExecution = new WorkflowExecution();
        workflowExecution.setExecutionID(UUID.randomUUID().toString());
        Workflow workflow = workflowServiceClient.getWorkflow(id);
        workflow.setStatus(WorkflowStatus.RUNNING);
        workflowServiceClient.updateWorkflow(workflow);
        workflowExecution.setWorkflowDescription(workflow.getDescription());
        workflowExecution.setWorkflowName(workflow.getName());
        workflowExecution.setWorkflowStatus(workflow.getStatus());
        return workflowExecutionRepository.save(workflowExecution);
    }


    @GetMapping("/stop/{executionId}")
    public WorkflowExecution stopWorkflow(@PathVariable String executionId) {
        WorkflowExecution workflowExecution = workflowExecutionRepository.findByExecutionID(executionId);
        Workflow workflow = workflowServiceClient.getWorkflow(workflowExecution.getWorkflowID());
        workflow.setStatus(WorkflowStatus.STOPPED);
        workflowServiceClient.updateWorkflow(workflow);
        workflowExecution.setWorkflowStatus(workflow.getStatus());
        return workflowExecutionRepository.save(workflowExecution);
    }

    @GetMapping("/pause/{executionId}")
    @ResponseStatus(HttpStatus.OK)
    public WorkflowExecution pauseWorkflow(@PathVariable String executionId) {
        WorkflowExecution workflowExecution = workflowExecutionRepository.findByExecutionID(executionId);
        Workflow workflow = workflowServiceClient.getWorkflow(workflowExecution.getWorkflowID());
        workflow.setStatus(WorkflowStatus.PAUSED);
        workflowServiceClient.updateWorkflow(workflow);
        workflowExecution.setWorkflowStatus(workflow.getStatus());
        return workflowExecutionRepository.save(workflowExecution);
    }

    @GetMapping("/resume/{executionId}")
    public WorkflowExecution resumeWorkflow(@PathVariable String executionId) {
        WorkflowExecution workflowExecution = workflowExecutionRepository.findByExecutionID(executionId);
        Workflow workflow = workflowServiceClient.getWorkflow(workflowExecution.getWorkflowID());
        workflow.setStatus(WorkflowStatus.RUNNING);
        workflowServiceClient.updateWorkflow(workflow);
        workflowExecution.setWorkflowStatus(workflow.getStatus());
        return workflowExecutionRepository.save(workflowExecution);
    }

}
