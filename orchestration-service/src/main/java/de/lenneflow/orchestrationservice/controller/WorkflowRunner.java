package de.lenneflow.orchestrationservice.controller;

import de.lenneflow.orchestrationservice.OrchestrationServiceApplication;
import de.lenneflow.orchestrationservice.enums.TaskStatus;
import de.lenneflow.orchestrationservice.enums.WorkFlowStepType;
import de.lenneflow.orchestrationservice.enums.WorkflowStatus;
import de.lenneflow.orchestrationservice.feignclients.TaskServiceClient;
import de.lenneflow.orchestrationservice.feignclients.WorkflowServiceClient;
import de.lenneflow.orchestrationservice.feignmodels.Task;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.model.WorkflowExecution;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import de.lenneflow.orchestrationservice.repository.WorkflowExecutionRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowInstanceRepository;
import de.lenneflow.orchestrationservice.repository.WorkflowStepInstanceRepository;
import de.lenneflow.orchestrationservice.utils.Util;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WorkflowRunner {

    final TaskServiceClient taskServiceClient;
    final WorkflowServiceClient workflowServiceClient;
    final WorkflowExecutionRepository workflowExecutionRepository;
    final WorkflowInstanceRepository workflowInstanceRepository;
    final WorkflowStepInstanceRepository workflowStepInstanceRepository;
    final QueueController queueController;
    final InstanceController instanceController;

    WorkflowRunner(TaskServiceClient taskServiceClient, WorkflowServiceClient workflowServiceClient, WorkflowExecutionRepository workflowExecutionRepository, WorkflowInstanceRepository workflowInstanceRepository, WorkflowStepInstanceRepository workflowStepInstanceRepository, QueueController queueController, InstanceController instanceController) {
        this.taskServiceClient = taskServiceClient;
        this.workflowServiceClient = workflowServiceClient;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
        this.queueController = queueController;
        this.instanceController = instanceController;
    }

    /**
     * Implementation of the Task results listener. This method will listen on the task results queue and process
     * the result task
     *
     * @param serializedTask serialized task object
     */
    @RabbitListener(queues = OrchestrationServiceApplication.TASKRESULTQUEUE)
    public void processTaskResult(byte[] serializedTask) {
        Task resultTask = Util.deserializeTask(serializedTask);
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(resultTask.getMetaData().get(Task.METADATA_KEY_EXECUTION_ID));
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(resultTask.getMetaData().get(Task.METADATA_KEY_WORKFlOW_INSTANCE_ID));
        WorkflowStepInstance stepInstance = workflowStepInstanceRepository.findByUid(resultTask.getMetaData().get(Task.METADATA_KEY_STEP_INSTANCE_ID));

        instanceController.updateWorkflowStepInstance(stepInstance, resultTask);

        if (stepInstance.getWorkFlowStepType() == WorkFlowStepType.TERMINATE) {
            terminateWorkflowRun(execution, workflowInstance, stepInstance);
        } else {
            switch (resultTask.getTaskStatus()) {
                case COMPLETED, SKIPPED:
                    WorkflowStepInstance nextStepInstance = getNextStepInstance(stepInstance, resultTask);
                    if (nextStepInstance != null) {
                        Task task = taskServiceClient.getTask(nextStepInstance.getTaskId());
                        Map<String, String> metadata = generateTaskMetaData(execution, workflowInstance, nextStepInstance);
                        task.setMetaData(metadata);
                        runStep(task, nextStepInstance);
                    }
                    break;
                case FAILED, TIMED_OUT:
                    workflowInstance.setErrorsPresent(true);
                    stepInstance.setErrorMessage(resultTask.getErrorMessage());
                    workflowStepInstanceRepository.save(stepInstance);
                    if (stepInstance.isRetriable() && stepInstance.getRetryCount() > 0) {
                        stepInstance.setRetryCount(stepInstance.getRetryCount() - 1);
                        workflowStepInstanceRepository.save(stepInstance);
                        Task task = taskServiceClient.getTask(stepInstance.getTaskId());
                        runStep(task, stepInstance);
                        break;
                    }
                    terminateWorkflowRun(execution, workflowInstance, stepInstance);
                    break;
                case CANCELED, FAILED_WITH_TERMINAL_ERROR:
                    workflowInstance.setErrorsPresent(true);
                    terminateWorkflowRun(execution, workflowInstance, stepInstance);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + resultTask.getTaskStatus());
            }
        }
    }

    /**
     * Method that terminates a workflow run. It will go throw all steps status and update the workflow instance status.
     *
     * @param execution        The workflow execution
     * @param workflowInstance the running workflow instance
     * @param stepInstance     the last step instance before termination
     */
    private void terminateWorkflowRun(WorkflowExecution execution, WorkflowInstance workflowInstance, WorkflowStepInstance stepInstance) {
        if (workflowInstance.isErrorsPresent()) {
            Map<String, String> errorMessages = new HashMap<>();
            for (String stepId : workflowInstance.getStepInstanceIds()) {
                WorkflowStepInstance foundStep = workflowStepInstanceRepository.findByUid(stepId);
                if (foundStep != null && foundStep.getErrorMessage() != null && !foundStep.getErrorMessage().isEmpty()) {
                    errorMessages.put(foundStep.getStepName(), foundStep.getErrorMessage());
                }
            }
            workflowInstance.setErrorMessages(errorMessages);
            workflowInstanceRepository.save(workflowInstance);
            switch (stepInstance.getTaskStatus()) {
                case FAILED, FAILED_WITH_TERMINAL_ERROR:
                    instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.FAILED);
                    instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.FAILED);
                    break;
                case CANCELED:
                    instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.STOPPED);
                    instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.STOPPED);
                    break;
                case TIMED_OUT:
                    instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.TIMED_OUT);
                    instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.TIMED_OUT);
                    break;
                default:
                    instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.COMPLETED_WITH_ERRORS);
                    instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.COMPLETED_WITH_ERRORS);
                    break;
            }
        } else {
            instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.COMPLETED);
            instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.COMPLETED);
        }

        //TODO delete workflow and step instances
    }


    /**
     * This is the start method of every workflow run. This method searches for the starting workflow step, gets the task
     * associated to the step and run the workflow step.
     *
     * @param workflowId      The ID od the workflow to run.
     * @param inputParameters The input parameters for the workflow run.
     * @return a workflow execution object.
     */
    public WorkflowExecution start(String workflowId, Map<String, Object> inputParameters) {
        WorkflowInstance workflowInstance = instanceController.newWorkflowInstance(workflowId, inputParameters);
        Workflow workflow = workflowServiceClient.getWorkflow(workflowId);
        WorkflowExecution execution = createWorkflowExecution(workflow, workflowInstance);
        instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.RUNNING);
        instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.RUNNING);

        WorkflowStepInstance firstStep = getStartStep(workflowInstance);
        assert firstStep != null;
        Task task = taskServiceClient.getTask(firstStep.getTaskId());
        Map<String, String> metadata = generateTaskMetaData(execution, workflowInstance, firstStep);
        task.setMetaData(metadata);
        runStep(task, firstStep);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    /**
     * Method that stops the workflow execution by updating the workflow instance and the workflow execution instance.
     *
     * @param workflowExecutionId The workflow execution ID.
     * @return a workflow execution object with the current status.
     */
    public WorkflowExecution stop(String workflowExecutionId) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(workflowExecutionId);
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(execution.getWorkflowInstanceId());
        instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.STOPPED);
        instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.STOPPED);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    /**
     * Method that pauses the workflow execution by updating the workflow instance and the workflow execution instance.
     *
     * @param workflowExecutionId The workflow execution ID.
     * @return a workflow execution object with the current status.
     */
    public WorkflowExecution pause(String workflowExecutionId) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(workflowExecutionId);
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(execution.getWorkflowInstanceId());
        instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.PAUSED);
        instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.PAUSED);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    /**
     * Method that resumes the workflow execution by updating the workflow instance and the workflow execution instance
     * and then running the next step of the workflow.
     *
     * @param workflowExecutionId The workflow execution ID.
     * @return a workflow execution object with the current status.
     */
    public WorkflowExecution resume(String workflowExecutionId) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(workflowExecutionId);
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(execution.getWorkflowInstanceId());
        instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.RUNNING);
        instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.RUNNING);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    /**
     * If called, this method will return the current state of the workflow execution.
     *
     * @param workflowExecutionId The workflow execution ID
     * @return a workflow execution object with the current status.
     */
    public WorkflowExecution executionState(String workflowExecutionId) {
        return workflowExecutionRepository.findByRunId(workflowExecutionId);
    }

    /**
     * Receives a workflow instance, searches for the start step and returns it.
     *
     * @param workflowInstance The workflow instance
     * @return the start step of the workflow.
     */
    private WorkflowStepInstance getStartStep(WorkflowInstance workflowInstance) {
        for (WorkflowStepInstance step : workflowStepInstanceRepository.findByWorkflowInstanceId(workflowInstance.getUid())) {
            if (step.getWorkFlowStepType() == WorkFlowStepType.START) return step;
        }
        return null;
    }

    /**
     * In order to enqueue the task it is necessary the group the important parameters in a metadata map, so the worker just
     * have to put it in the result object. The information are necessary to locate which running instance belongs to
     * the result task.
     *
     * @param execution            the workflow execution ID.
     * @param workflowInstance     The running workflow instance
     * @param workflowStepInstance the running workflow step
     * @return the generated map of metadata.
     */
    private Map<String, String> generateTaskMetaData(WorkflowExecution execution, WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put(Task.METADATA_KEY_EXECUTION_ID, execution.getRunId());
        metaData.put(Task.METADATA_KEY_WORKFlOW_INSTANCE_ID, workflowInstance.getUid());
        metaData.put(Task.METADATA_KEY_STEP_INSTANCE_ID, workflowStepInstance.getUid());
        return metaData;
    }

    /**
     * This method creates a workflow execution instance.
     *
     * @param workflow         The workflow object.
     * @param workflowInstance The workflow instance object.
     * @return a workflow execution object
     */
    private WorkflowExecution createWorkflowExecution(Workflow workflow, WorkflowInstance workflowInstance) {
        List<WorkflowStepInstance> stepInstances = new ArrayList<>();
        for (String setInstanceId : workflowInstance.getStepInstanceIds()) {
            stepInstances.add(workflowStepInstanceRepository.findByUid(setInstanceId));
        }
        WorkflowExecution workflowExecution = new WorkflowExecution(workflow, workflowInstance, stepInstances);
        return workflowExecutionRepository.save(workflowExecution);
    }

    /**
     * Runs a workflow step by adding it to the queue.
     *
     * @param task task to process.
     * @param step workflow step to run
     */
    private void runStep(Task task, WorkflowStepInstance step) {
        queueController.addWorkerTaskToQueue(task);
        instanceController.updateWorkflowStepInstanceStatus(step, TaskStatus.IN_PROGRESS);
    }


    /**
     * Finds the next workflow step instance to run.
     *
     * @param stepInstance the current step instance.
     * @param task         the current executed task.
     * @return the next workflow step instance.
     */
    private WorkflowStepInstance getNextStepInstance(WorkflowStepInstance stepInstance, Task task) {
        switch (stepInstance.getWorkFlowStepType()) {
            case SIMPLE, START:
                return workflowStepInstanceRepository.findByUid(stepInstance.getNextStepId());
            case DO_WHILE:
                if (task.isDoWhileStop())
                    return workflowStepInstanceRepository.findByUid(stepInstance.getNextStepId());
                else
                    return stepInstance;
            case SWITCH:
                String stepInstanceId = stepInstance.getDecisionCases().get(task.getSwitchCase());
                return workflowStepInstanceRepository.findByUid(stepInstanceId);
            default:
                return null;
        }

    }
}
