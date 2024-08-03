package de.lenneflow.orchestrationservice.controller;

import de.lenneflow.orchestrationservice.enums.FunctionStatus;
import de.lenneflow.orchestrationservice.enums.WorkflowStatus;
import de.lenneflow.orchestrationservice.feignclients.FunctionServiceClient;
import de.lenneflow.orchestrationservice.feignclients.WorkflowServiceClient;
import de.lenneflow.orchestrationservice.feignmodels.Function;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.feignmodels.WorkflowStep;
import de.lenneflow.orchestrationservice.model.WorkflowExecution;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import de.lenneflow.orchestrationservice.repository.WorkflowExecutionRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class InstanceController {

    final FunctionServiceClient functionServiceClient;
    final WorkflowServiceClient workflowServiceClient;
    final WorkflowExecutionRepository workflowExecutionRepository;
    final WorkflowInstanceRepository workflowInstanceRepository;
    final WorkflowStepInstanceRepository workflowStepInstanceRepository;
    final QueueController queueController;

    public InstanceController(FunctionServiceClient functionServiceClient, WorkflowServiceClient workflowServiceClient, WorkflowExecutionRepository workflowExecutionRepository, WorkflowInstanceRepository workflowInstanceRepository, WorkflowStepInstanceRepository workflowStepInstanceRepository, QueueController queueController) {
        this.functionServiceClient = functionServiceClient;
        this.workflowServiceClient = workflowServiceClient;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
        this.queueController = queueController;
    }

    /**
     * From a workflow ID, this method will create a new workflow instance to run.
     * It will also create all the workflow step instances.
     *
     * @param workflowId      workflow ID.
     * @param inputParameters the specific input parameters.
     * @return the created workflow instance.
     */
    public WorkflowInstance newWorkflowInstance(String workflowId, Map<String, Object> inputParameters) {
        Workflow workflow = workflowServiceClient.getWorkflow(workflowId);
        WorkflowInstance workflowInstance = new WorkflowInstance(workflow, inputParameters);
        workflowInstanceRepository.save(workflowInstance);
        workflowInstance.setStatus(WorkflowStatus.NOT_RUN);
        List<WorkflowStep> steps = workflowServiceClient.getWorkflowSteps(workflowId);
        Map<String, String> stepStepInstanceMapping = new HashMap<>();

        for (WorkflowStep step : steps) {
            WorkflowStepInstance stepInstance = new WorkflowStepInstance(step, workflowInstance.getUid());
            stepStepInstanceMapping.put(step.getUid(), stepInstance.getUid());
            workflowStepInstanceRepository.save(stepInstance);
        }
        List<String> stepInstanceIds = updateWorkflowStepInstances(workflowId, steps, stepStepInstanceMapping);
        workflowInstance.setStepInstanceIds(stepInstanceIds);
        return workflowInstanceRepository.save(workflowInstance);
    }


    /**
     * Updates the workflow step instance status and output data.
     * @param workflowStepInstance the workflow step instance to update.
     * @param function the executed function belonging to the workflow instance.
     */
    public void updateWorkflowStepInstance(WorkflowStepInstance workflowStepInstance, Function function) {

        workflowStepInstance.setFunctionStatus(function.getFunctionStatus());
        workflowStepInstanceRepository.save(workflowStepInstance);

        Map<String, Object> output = function.getOutputData();
        workflowStepInstance.setOutputData(output);
        workflowStepInstanceRepository.save(workflowStepInstance);
    }

    /**
     * Updated the workflow instance status
     * @param workflowInstance the workflow instance to update.
     * @param workflowStatus The status to set.
     */
    public void updateWorkflowInstanceStatus(WorkflowInstance workflowInstance, WorkflowStatus workflowStatus) {
        workflowInstance.setStatus(workflowStatus);
        workflowInstanceRepository.save(workflowInstance);
    }

    /**
     * Updated the workflow execution status
     * @param execution the workflow execution to update.
     * @param workflowStatus The status to set.
     */
    public void updateWorkflowExecutionStatus(WorkflowExecution execution, WorkflowStatus workflowStatus) {
        execution.setWorkflowStatus(workflowStatus);
        workflowExecutionRepository.save(execution);
    }

    /**
     * Updated the workflow step instance status
     * @param stepInstance the workflow step instance to update.
     * @param functionStatus The status to set.
     */
    public void updateWorkflowStepInstanceStatus(WorkflowStepInstance stepInstance, FunctionStatus functionStatus) {
        stepInstance.setFunctionStatus(functionStatus);
        workflowStepInstanceRepository.save(stepInstance);
    }


    /**
     * This method sets previous and next step parameters to the steps in the given list.
     * @param workflowId The workflow ID.
     * @param steps The workflow step list to update.
     * @param stepStepInstanceMapping a map of workflow instances
     * @return a list of workflow instance IDs.
     */
    private List<String> updateWorkflowStepInstances(String workflowId, List<WorkflowStep> steps, Map<String, String> stepStepInstanceMapping) {
        List<String> stepInstanceIds = new ArrayList<>();
        for (WorkflowStep step : steps) {
            WorkflowStepInstance stepInstance = workflowStepInstanceRepository.findByUid(stepStepInstanceMapping.get(step.getUid()));
            WorkflowStep nextStep = workflowServiceClient.getWorkflowStep(step.getNextStepId());
            if (nextStep != null) {
                WorkflowStepInstance nextStepInstance = workflowStepInstanceRepository.findByUid(stepStepInstanceMapping.get(step.getNextStepId()));
                stepInstance.setNextStepId(nextStepInstance.getUid());
            }
            WorkflowStep previousStep = workflowServiceClient.getWorkflowStep(step.getPreviousStepId());
            if (previousStep != null) {
                WorkflowStepInstance previousStepInstance = workflowStepInstanceRepository.findByUid(stepStepInstanceMapping.get(step.getPreviousStepId()));
                stepInstance.setPreviousStepId(previousStepInstance.getUid());
            }
            Map<String, String> decisionCases = step.getDecisionCases();
            if (decisionCases != null && !decisionCases.isEmpty()) {
                Map<String, String> decisionCaseInstances = new HashMap<>();
                for (Map.Entry<String, String> entry : decisionCases.entrySet()) {
                    String decisionCaseId = entry.getValue();
                    WorkflowStepInstance decisionCaseInstance = workflowStepInstanceRepository.findByUid(stepStepInstanceMapping.get(decisionCaseId));
                    decisionCaseInstances.put(entry.getKey(), decisionCaseInstance.getUid());
                }
                stepInstance.setDecisionCases(decisionCaseInstances);
            }
            workflowStepInstanceRepository.save(stepInstance);
            stepInstanceIds.add(stepInstance.getUid());
        }
        return stepInstanceIds;
    }
}
