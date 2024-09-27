package de.lenneflow.orchestrationservice.component;

import de.lenneflow.orchestrationservice.dto.FunctionDto;
import de.lenneflow.orchestrationservice.enums.RunOrderLabel;
import de.lenneflow.orchestrationservice.enums.RunStatus;
import de.lenneflow.orchestrationservice.exception.InternalServiceException;
import de.lenneflow.orchestrationservice.feignclients.FunctionServiceClient;
import de.lenneflow.orchestrationservice.feignclients.WorkflowServiceClient;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.feignmodels.WorkflowStep;
import de.lenneflow.orchestrationservice.model.WorkflowExecution;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import de.lenneflow.orchestrationservice.repository.WorkflowExecutionRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import de.lenneflow.orchestrationservice.utils.ExpressionEvaluator;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
public class InstanceController {

    final FunctionServiceClient functionServiceClient;
    final WorkflowServiceClient workflowServiceClient;
    final WorkflowExecutionRepository workflowExecutionRepository;
    final WorkflowInstanceRepository workflowInstanceRepository;
    final WorkflowStepInstanceRepository workflowStepInstanceRepository;
    final QueueController queueController;
    final ExpressionEvaluator expressionEvaluator;

    public InstanceController(FunctionServiceClient functionServiceClient, WorkflowServiceClient workflowServiceClient, WorkflowExecutionRepository workflowExecutionRepository, WorkflowInstanceRepository workflowInstanceRepository, WorkflowStepInstanceRepository workflowStepInstanceRepository, QueueController queueController, ExpressionEvaluator expressionEvaluator) {
        this.functionServiceClient = functionServiceClient;
        this.workflowServiceClient = workflowServiceClient;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
        this.queueController = queueController;
        this.expressionEvaluator = expressionEvaluator;
    }

    /**
     * From a workflow ID, this method will create a new workflow instance to run.
     * It will also create all the workflow step instances.
     *
     * @param workflowId      workflow ID.
     * @param inputData the specific input parameters.
     * @return the created workflow instance.
     */
    public WorkflowInstance createWorkflowInstance(String workflowId, Map<String, Object> inputData) {
        Workflow workflow = workflowServiceClient.getWorkflowById(workflowId);
        WorkflowInstance workflowInstance = new WorkflowInstance(workflow);
        workflowInstanceRepository.save(workflowInstance);
        workflowInstance.setRunStatus(RunStatus.NEW);
        List<WorkflowStepInstance> stepInstances = createWorkflowStepInstances(workflowInstance, inputData);
        workflowInstance.setStepInstances(stepInstances);
        return workflowInstanceRepository.save(workflowInstance);
    }

    private List<WorkflowStepInstance> createWorkflowStepInstances(WorkflowInstance workflowInstance, Map<String, Object> inputData) {
        List<WorkflowStepInstance> workflowStepInstances = new ArrayList<>();
        List<WorkflowStepInstance> result = new ArrayList<>();
        List<WorkflowStep> steps = workflowServiceClient.getStepListByWorkflowId(workflowInstance.getWorkflowUid());
        List<WorkflowStep> sorted = steps.stream().sorted(Comparator.comparing(WorkflowStep::getExecutionOrder)).toList();
        for (WorkflowStep step : sorted) {
            WorkflowStepInstance stepInstance = new WorkflowStepInstance(step, workflowInstance.getUid());
            workflowStepInstances.add(stepInstance);
        }
        for (int i = 0; i < sorted.size(); i++) {
            WorkflowStepInstance stepInstance = workflowStepInstances.get(i);
            if (i == 0) {
                stepInstance.setRunOrderLabel(RunOrderLabel.FIRST);
                stepInstance.setNextStepId(workflowStepInstances.get(i + 1).getUid());
                if(inputData != null && !inputData.isEmpty()){
                    stepInstance.setInputData(inputData);
                }
                result.add(workflowStepInstanceRepository.save(stepInstance));
            } else if (i == workflowStepInstances.size() - 1) {
                stepInstance.setRunOrderLabel(RunOrderLabel.LAST);
                stepInstance.setPreviousStepId(workflowStepInstances.get(i - 1).getUid());
                result.add(workflowStepInstanceRepository.save(stepInstance));
            } else {
                stepInstance.setRunOrderLabel(RunOrderLabel.INTERMEDIATE);
                stepInstance.setNextStepId(workflowStepInstances.get(i + 1).getUid());
                stepInstance.setPreviousStepId(workflowStepInstances.get(i - 1).getUid());
                result.add(workflowStepInstanceRepository.save(stepInstance));
            }
        }
        return result;
    }


    /**
     * Updates the workflow step instance status and output data.
     *
     * @param workflowStepInstance the workflow step instance to update.
     * @param functionDto             the executed functionDto belonging to the workflow instance.
     */
    public void updateWorkflowStepInstance(WorkflowStepInstance workflowStepInstance, FunctionDto functionDto) {
        workflowStepInstance.setRunStatus(functionDto.getRunStatus());
        workflowStepInstanceRepository.save(workflowStepInstance);
        Map<String, Object> output = functionDto.getOutputData();
        workflowStepInstance.setOutputData(output);
        workflowStepInstanceRepository.save(workflowStepInstance);
    }

    public void updateWorkflowInstanceAndExecutionStatus(WorkflowInstance workflowInstance, WorkflowExecution execution, RunStatus runStatus) {
        updateWorkflowInstanceStatus(workflowInstance, runStatus);
        updateWorkflowExecutionStatus(execution, runStatus);
    }

    /**
     * Updated the workflow instance status
     *
     * @param workflowInstance the workflow instance to update.
     * @param runStatus the run status
     */
    public void updateWorkflowInstanceStatus(WorkflowInstance workflowInstance, RunStatus runStatus) {
        workflowInstance.setRunStatus(runStatus);
        workflowInstanceRepository.save(workflowInstance);
    }

    /**
     * Updated the workflow execution status
     *
     * @param execution the workflow execution to update.
     * @param runStatus The status to set.
     */
    public void updateWorkflowExecutionStatus(WorkflowExecution execution, RunStatus runStatus) {
        execution.setRunStatus(runStatus);
        workflowExecutionRepository.save(execution);
    }

    /**
     * Updated the workflow step instance status
     *
     * @param stepInstance the workflow step instance to update.
     * @param runStatus    The status to set.
     */
    public void updateWorkflowStepInstanceStatus(WorkflowStepInstance stepInstance, RunStatus runStatus) {
        stepInstance.setRunStatus(runStatus);
        workflowStepInstanceRepository.save(stepInstance);
    }

    /**
     * Finds the next workflow step instance to run.
     *
     * @param stepInstance the current step instance.
     * @return the next workflow step instance.
     */
    public WorkflowStepInstance getNextWorkflowStepInstance(WorkflowStepInstance stepInstance) {
        switch (stepInstance.getControlStructure()) {
            case SIMPLE, SUB_WORKFLOW:
                return workflowStepInstanceRepository.findByUid(stepInstance.getNextStepId());
            case DO_WHILE:
                if (expressionEvaluator.evaluateBooleanExpression(stepInstance.getWorkflowInstanceUid(), stepInstance.getStopCondition()))
                    return workflowStepInstanceRepository.findByUid(stepInstance.getNextStepId());
                else
                    return stepInstance;
            case SWITCH:
                String switchCondition = expressionEvaluator.evaluateStringExpression(stepInstance.getWorkflowInstanceUid(), stepInstance.getSwitchCondition());
                WorkflowStepInstance foundStepInstance = stepInstance.getDecisionCases().get(switchCondition);
                if (foundStepInstance == null) {
                    WorkflowStepInstance defaultStepInstance = stepInstance.getDecisionCases().get("Default");
                    if (defaultStepInstance == null) {
                        throw new InternalServiceException("The next workflow step to execute could not be found after the evaluation of the switch condition " + stepInstance.getSwitchCondition());
                    } else {
                        return defaultStepInstance;
                    }
                }
                return foundStepInstance;
            default:
                throw new InternalServiceException("The next workflow step to execute could not be found");
        }

    }

    /**
     * Receives a workflow instance, searches for the start step and returns it.
     *
     * @param workflowInstance The workflow instance
     * @return the start step of the workflow.
     */
    public WorkflowStepInstance getStartStep(WorkflowInstance workflowInstance) {
        for (WorkflowStepInstance step : workflowStepInstanceRepository.findByWorkflowInstanceUid(workflowInstance.getUid())) {
            if (step.getRunOrderLabel() == RunOrderLabel.FIRST) return step;
        }
        throw new InternalServiceException("The first workflow step to execute could not be found");
    }

}


