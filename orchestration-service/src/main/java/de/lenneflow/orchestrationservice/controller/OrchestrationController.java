package de.lenneflow.orchestrationservice.controller;

import de.lenneflow.orchestrationservice.exception.PayloadNotValidException;
import de.lenneflow.orchestrationservice.feignclients.FunctionServiceClient;
import de.lenneflow.orchestrationservice.feignclients.WorkflowServiceClient;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.model.GlobalInputData;
import de.lenneflow.orchestrationservice.model.WorkflowExecution;
import de.lenneflow.orchestrationservice.repository.GlobalInputDataRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowExecutionRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import de.lenneflow.orchestrationservice.helpercomponents.WorkflowRunner;
import de.lenneflow.orchestrationservice.utils.Validator;
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
    final GlobalInputDataRepository globalInputDataRepository;

    public OrchestrationController(WorkflowExecutionRepository workflowExecutionRepository, WorkflowServiceClient workflowServiceClient, FunctionServiceClient functionServiceClient, WorkflowInstanceRepository workflowInstanceRepository, WorkflowStepInstanceRepository workflowStepInstanceRepository, WorkflowRunner workflowRunner, GlobalInputDataRepository globalInputDataRepository) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowServiceClient = workflowServiceClient;
        this.functionServiceClient = functionServiceClient;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
        this.workflowRunner = workflowRunner;
        this.globalInputDataRepository = globalInputDataRepository;
    }

    @GetMapping("/workflows/{workflow-id}/input-data/{input-data-id}/start")
    public WorkflowExecution startWorkflowGet(@PathVariable(name = "workflow-id") String workflowId, @PathVariable("input-data-id") String inputdataId) {
        GlobalInputData globalInputData = globalInputDataRepository.findByUid(inputdataId);
        Workflow workflow = workflowServiceClient.getWorkflowById(workflowId);
        if (globalInputData == null) {
            throw new PayloadNotValidException("Could not find global input data with id " + inputdataId);
        }
        if (workflow == null) {
            throw new PayloadNotValidException("Could not find workflow with id " + workflowId);
        }
        Validator.validateInputOutputData(workflow.getInputDataSchema().getSchema(), workflow.getInputDataSchema().getSchemaVersion(), globalInputData.getInputData());
        return workflowRunner.startWorkflow(workflowId, globalInputData.getInputData());
    }

    @PostMapping("/workflows/{workflow-id}/start")
    public WorkflowExecution startWorkflowPost(@PathVariable(name = "workflow-id") String workflowId, @RequestBody Map<String, Object> inputData) {
        Workflow workflow = workflowServiceClient.getWorkflowById(workflowId);
        if (workflow == null) {
            throw new PayloadNotValidException("Could not find workflow with id " + workflowId);
        }
        Validator.validateInputOutputData(workflow.getInputDataSchema().getSchema(), workflow.getInputDataSchema().getSchemaVersion(), inputData);
        return workflowRunner.startWorkflow(workflowId, inputData);
    }

    @GetMapping("/workflows/runs/{run-id}/stop")
    public WorkflowExecution stopWorkflow(@PathVariable(name = "run-id") String executionId) {
        return workflowRunner.stopWorkflow(executionId);
    }

    @GetMapping("/workflows/runs/{run-id}/pause")
    @ResponseStatus(HttpStatus.OK)
    public WorkflowExecution pauseWorkflow(@PathVariable(name = "run-id") String executionId) {
        return workflowRunner.pauseWorkflow(executionId);
    }

    @GetMapping("/workflows/runs/{run-id}/resume")
    public WorkflowExecution resumeWorkflow(@PathVariable(name = "run-id") String executionId) {
        return workflowRunner.resumeWorkflow(executionId);
    }

    @GetMapping("/workflows/runs/{run-id}/state")
    public WorkflowExecution workflowRunState(@PathVariable(name = "run-id") String executionId) {
        return workflowRunner.getCurrentExecutionState(executionId);
    }

    @GetMapping("/workflows/runs")
    public List<WorkflowExecution> executionList() {
        return workflowExecutionRepository.findAll();
    }

    @PostMapping("/workflows/input-data")
    public GlobalInputData createGlobalInputData(@RequestBody GlobalInputData globalInputData) {
        Validator.validate(globalInputData);
        return globalInputDataRepository.save(globalInputData);
    }

    @PostMapping("/workflows/input-data/{input-data-uid}")
    public GlobalInputData updateGlobalInputData(@RequestBody GlobalInputData globalInputData, @PathVariable("input-data-uid") String inputDataUid) {
        Validator.validate(globalInputData);
        GlobalInputData found = globalInputDataRepository.findByUid(inputDataUid);
        if (found == null) {
            throw new PayloadNotValidException("Could not find global input data with id " + inputDataUid);
        }
        globalInputData.setUid(inputDataUid);
        return globalInputDataRepository.save(globalInputData);
    }

    @GetMapping("/workflows/input-data/{input-data-uid}")
    public GlobalInputData getGlobalInputData(@PathVariable("input-data-uid") String inputDataUid) {
        GlobalInputData found = globalInputDataRepository.findByUid(inputDataUid);
        if (found == null) {
            throw new PayloadNotValidException("Input data with id " + inputDataUid + " not found");
        }
        return found;
    }

}
