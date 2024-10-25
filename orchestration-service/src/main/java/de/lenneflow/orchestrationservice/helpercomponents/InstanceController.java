package de.lenneflow.orchestrationservice.helpercomponents;

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

import java.time.LocalDateTime;
import java.util.*;

/**
 * This is the controller class for workflow and step instances.
 * For every run, instances of workflow and workflow steps are created.
 * Instances here are normal persisted entities.
 *
 * @author Idrissa Ganemtore
 */
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
     * @param workflowId workflow ID.
     * @param inputData  the specific input parameters.
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

    /**
     * create the list of workflow step instances for a given workflow instance.
     *
     * @param workflowInstance the workflow instance
     * @param inputData        the input data of the workflow
     * @return the workflow step instances list.
     */
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
                if (inputData != null && !inputData.isEmpty()) {
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
     * @param functionDto          the executed functionDto belonging to the workflow instance.
     */
    public void updateWorkflowStepInstance(WorkflowStepInstance workflowStepInstance, FunctionDto functionDto) {
        workflowStepInstance.setRunStatus(functionDto.getRunStatus());
        workflowStepInstanceRepository.save(workflowStepInstance);
        Map<String, Object> output = functionDto.getOutputData();
        workflowStepInstance.setOutputData(output);
        workflowStepInstance.setRunCount(workflowStepInstance.getRunCount() + 1);
        if (functionDto.getFailureReason() != null && !functionDto.getFailureReason().isEmpty()) {
            workflowStepInstance.setFailureReason(functionDto.getFailureReason());
        }
        workflowStepInstanceRepository.save(workflowStepInstance);
    }

    /**
     * Updates the status of workflow instance and workflow execution
     *
     * @param workflowInstance the workflow instance
     * @param execution        the workflow execution
     * @param runStatus        the status
     */
    public void updateWorkflowInstanceAndExecutionStatus(WorkflowInstance workflowInstance, WorkflowExecution execution, RunStatus runStatus) {
        updateWorkflowInstanceStatus(workflowInstance, runStatus);
        updateWorkflowExecutionStatus(execution, runStatus);
    }

    /**
     * Sets the finished time of a workflow run.
     *
     * @param workflowInstance the workflow instance
     * @param execution        the workflow execution
     */
    public void setWorkflowRunEndTime(WorkflowInstance workflowInstance, WorkflowExecution execution) {
        workflowInstance.setEndTime(LocalDateTime.now());
        workflowInstanceRepository.save(workflowInstance);
        execution.setEndTime(LocalDateTime.now());
        workflowExecutionRepository.save(execution);
    }

    /**
     * Sets the failure reason to the workflow and to the workflow execution
     *
     * @param workflowInstance the workflow instance
     * @param execution        the workflow execution
     * @param failureReason    the failure reason
     */
    public void setFailureReason(WorkflowInstance workflowInstance, WorkflowExecution execution, String failureReason) {
        workflowInstance.setFailureReason(failureReason);
        workflowInstanceRepository.save(workflowInstance);
        execution.setFailureReason(failureReason);
        workflowExecutionRepository.save(execution);
    }


    /**
     * This function deletes old workflow runs.
     *
     * @param keepDaysCount     the max number of days to keep runs.
     * @param maxInstancesCount the max number of runs to keep.
     */
    public void deleteLastWorkflowInstances(int keepDaysCount, int maxInstancesCount) {
        LocalDateTime now = LocalDateTime.now();
        List<WorkflowExecution> executionsToDelete = new ArrayList<>();
        List<WorkflowInstance> instancesToDelete = new ArrayList<>();
        List<WorkflowExecution> instances = workflowExecutionRepository.findAll();

        //Sort from oldest to newest
        List<WorkflowExecution> sortedExecutions = new ArrayList<>(instances.stream().sorted((o1, o2) -> {
            if (o1.getStartTime().isBefore(o2.getStartTime())) {
                return -1;
            }
            if (o1.getStartTime().isAfter(o2.getStartTime())) {
                return 1;
            }
            return 0;
        }).toList());

        //iterate over the sorted executions and add the oldest to the list to remove
        for (WorkflowExecution execution : sortedExecutions) {
            if (execution.getStartTime().plusDays(keepDaysCount).isBefore(now)) {
                executionsToDelete.add(execution);
                instancesToDelete.add(workflowInstanceRepository.findByUid(execution.getWorkflowInstanceId()));
            } else {
                if (sortedExecutions.size() - executionsToDelete.size() >= maxInstancesCount) {
                    executionsToDelete.add(execution);
                    instancesToDelete.add(workflowInstanceRepository.findByUid(execution.getWorkflowInstanceId()));
                } else {
                    break;
                }
            }
        }
        //Delete objects from database
        for (WorkflowInstance instance : instancesToDelete) {
            List<WorkflowStepInstance> stepInstances = workflowStepInstanceRepository.findByWorkflowInstanceUid(instance.getUid());
            workflowStepInstanceRepository.deleteAll(stepInstances);
            workflowInstanceRepository.delete(instance);
        }
        workflowExecutionRepository.deleteAll(executionsToDelete);
    }


    /**
     * Updated the workflow instance status
     *
     * @param workflowInstance the workflow instance to update.
     * @param runStatus        the run status
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
            case SIMPLE, SUB_WORKFLOW, SWITCH:
                return workflowStepInstanceRepository.findByUid(stepInstance.getNextStepId());
            case DO_WHILE:
                if (expressionEvaluator.evaluateBooleanExpression(stepInstance.getWorkflowInstanceUid(), stepInstance.getStopCondition()))
                    return workflowStepInstanceRepository.findByUid(stepInstance.getNextStepId());
                else
                    return stepInstance;
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


