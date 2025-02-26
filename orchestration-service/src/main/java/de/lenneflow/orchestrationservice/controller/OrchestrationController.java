package de.lenneflow.orchestrationservice.controller;

import de.lenneflow.orchestrationservice.dto.GlobalInputDataDto;
import de.lenneflow.orchestrationservice.exception.PayloadNotValidException;
import de.lenneflow.orchestrationservice.feignclients.WorkflowServiceClient;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.helpercomponents.InstanceController;
import de.lenneflow.orchestrationservice.model.GlobalInputData;
import de.lenneflow.orchestrationservice.dto.WorkflowExecution;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import de.lenneflow.orchestrationservice.repository.GlobalInputDataRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.helpercomponents.WorkflowRunner;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import de.lenneflow.orchestrationservice.utils.ObjectMapper;
import de.lenneflow.orchestrationservice.utils.Validator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Rest controller for the orchestration service
 *
 * @author Idrissa Ganemtore
 */
@RestController
@RequestMapping("/api/control")
@Tag(name = "Workflow Run API")
@RequiredArgsConstructor
public class OrchestrationController {

    final WorkflowServiceClient workflowServiceClient;
    final WorkflowInstanceRepository workflowInstanceRepository;
    final WorkflowStepInstanceRepository workflowStepInstanceRepository;
    final WorkflowRunner workflowRunner;
    final InstanceController instanceController;
    final GlobalInputDataRepository globalInputDataRepository;

    @Operation(summary = "Starts a workflow by UID")
    @GetMapping("/workflow/{workflow-uid}/input-data/{input-data-uid}/start")
    public WorkflowExecution startWorkflowGet(@PathVariable(name = "workflow-uid") @Parameter(name = "Workflow UID") String workflowId, @PathVariable("input-data-uid") @Parameter(name = "Input data UID") String inputdataId) {
        GlobalInputData globalInputData = globalInputDataRepository.findByUid(inputdataId);
        Workflow workflow = workflowServiceClient.getWorkflowById(workflowId);
        if (globalInputData == null) {
            throw new PayloadNotValidException("Could not find global input data with id " + inputdataId);
        }
        if (workflow == null) {
            throw new PayloadNotValidException("Could not find a workflow with id " + workflowId);
        }
        Validator.validateJsonData(workflow.getInputDataSchema().getSchema(), workflow.getInputDataSchema().getSchemaVersion(), globalInputData.getInputData());

        //create an instance for the workflow
        WorkflowInstance workflowInstance = instanceController.generateWorkflowInstance(workflow, globalInputData.getInputData(), null, null);

        return workflowRunner.startWorkflow(workflowInstance);
    }

    @Operation(summary = "Starts a workflow by UID")
    @GetMapping("/workflow/{workflow-uid}/start")
    public WorkflowExecution startWorkflowGet2(@PathVariable(name = "workflow-uid") String workflowId) {
        Workflow workflow = workflowServiceClient.getWorkflowById(workflowId);
        if (workflow == null) {
            throw new PayloadNotValidException("Could not find workflow with id " + workflowId);
        }
        //create an instance for the workflow
        WorkflowInstance workflowInstance = instanceController.generateWorkflowInstance(workflow, null, null, null);
        return workflowRunner.startWorkflow(workflowInstance);
    }

    @Operation(summary = "Starts a workflow by UID")
    @PostMapping("/workflow/{workflow-uid}/start")
    public WorkflowExecution startWorkflowPost(@PathVariable(name = "workflow-uid") String workflowId, @RequestBody Map<String, Object> inputData) {
        Workflow workflow = workflowServiceClient.getWorkflowById(workflowId);
        if (workflow == null) {
            throw new PayloadNotValidException("Could not find workflow with id " + workflowId);
        }
        Validator.validateJsonData(workflow.getInputDataSchema().getSchema(), workflow.getInputDataSchema().getSchemaVersion(), inputData);

        //create an instance for the workflow
        WorkflowInstance workflowInstance = instanceController.generateWorkflowInstance(workflow, inputData, null, null);

        return workflowRunner.startWorkflow(workflowInstance);
    }

    @GetMapping("/workflow/run/{uid}/stop")
    public WorkflowExecution stopWorkflow(@PathVariable(name = "uid") String executionId) {
        return workflowRunner.stopWorkflow(executionId);
    }

    @GetMapping("/workflow/run/{uid}/pause")
    @ResponseStatus(HttpStatus.OK)
    public WorkflowExecution pauseWorkflow(@PathVariable(name = "uid") String executionId) {
        return workflowRunner.pauseWorkflow(executionId);
    }

    @GetMapping("/workflow/run/{uid}/resume")
    public WorkflowExecution resumeWorkflow(@PathVariable(name = "uid") String executionId) {
        return workflowRunner.resumeWorkflow(executionId);
    }

    @GetMapping("/workflow/run/{uid}/state")
    public WorkflowExecution workflowRunState(@PathVariable(name = "uid") String executionId) {
        return workflowRunner.getCurrentExecutionState(executionId);
    }

    @GetMapping("/workflow/run/list")
    public List<WorkflowExecution> executionList() {
        return getWorkflowExecutionList();
    }

    @DeleteMapping("/workflow/run/{uid}")
    public void deleteWorkflowRun(@PathVariable String uid) {
        WorkflowInstance instance = workflowInstanceRepository.findByUid(uid);
        if (instance == null) {
            throw new PayloadNotValidException("Could not find workflow instance with id " + uid);
        }
        workflowStepInstanceRepository.deleteAll(instance.getStepInstances());
        workflowInstanceRepository.delete(instance);
    }

    @PostMapping("/workflow/input-data/create")
    public GlobalInputData createGlobalInputData(@RequestBody GlobalInputDataDto globalInputDataDto) {
        GlobalInputData globalInputData = ObjectMapper.mapToGlobalInputData(globalInputDataDto);
        Validator.validate(globalInputData);
        globalInputData.setUid(UUID.randomUUID().toString());
        return globalInputDataRepository.save(globalInputData);
    }

    @PostMapping("/workflow/input-data/{uid}/update")
    public GlobalInputData updateGlobalInputData(@RequestBody GlobalInputDataDto globalInputDataDto, @PathVariable("uid") String inputDataUid) {
        GlobalInputData globalInputData = ObjectMapper.mapToGlobalInputData(globalInputDataDto);
        Validator.validate(globalInputData);
        GlobalInputData found = globalInputDataRepository.findByUid(inputDataUid);
        if (found == null) {
            throw new PayloadNotValidException("Could not find global input data with id " + inputDataUid);
        }
        globalInputData.setUid(inputDataUid);
        return globalInputDataRepository.save(globalInputData);
    }

    @GetMapping("/workflow/input-data/{uid}")
    public GlobalInputData getGlobalInputData(@PathVariable("uid") String inputDataUid) {
        GlobalInputData found = globalInputDataRepository.findByUid(inputDataUid);
        if (found == null) {
            throw new PayloadNotValidException("Input data with id " + inputDataUid + " not found");
        }
        return found;
    }

    private List<WorkflowExecution> getWorkflowExecutionList(){
        List<WorkflowExecution> runList = new ArrayList<>();
        List<WorkflowInstance> instances = workflowInstanceRepository.findAll();
        for (WorkflowInstance instance : instances) {
            runList.add(new WorkflowExecution(instance));
        }
        return runList;
    }

}
