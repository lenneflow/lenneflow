package de.lenneflow.executionservice.controller;

import de.lenneflow.executionservice.ExecutionServiceApplication;
import de.lenneflow.executionservice.enums.TaskStatus;
import de.lenneflow.executionservice.enums.WorkFlowStepType;
import de.lenneflow.executionservice.enums.WorkflowStatus;
import de.lenneflow.executionservice.feignclients.TaskServiceClient;
import de.lenneflow.executionservice.feignclients.WorkflowServiceClient;
import de.lenneflow.executionservice.feignmodels.Task;
import de.lenneflow.executionservice.feignmodels.Workflow;
import de.lenneflow.executionservice.model.WorkflowExecution;
import de.lenneflow.executionservice.model.WorkflowInstance;
import de.lenneflow.executionservice.model.WorkflowStepInstance;
import de.lenneflow.executionservice.repository.WorkflowExecutionRepository;
import de.lenneflow.executionservice.repository.WorkflowInstanceRepository;
import de.lenneflow.executionservice.repository.WorkflowStepInstanceRepository;
import de.lenneflow.executionservice.utils.Util;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.*;

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

    @RabbitListener(queues = ExecutionServiceApplication.TASKRESULTQUEUE)
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

    private void terminateWorkflowRun(WorkflowExecution execution, WorkflowInstance workflowInstance, WorkflowStepInstance stepInstance) {
        if (workflowInstance.isErrorsPresent()){
            Map<String, String> errorMessages =  new HashMap<>();
            for(String stepId: workflowInstance.getStepInstanceIds()){
                WorkflowStepInstance foundStep = workflowStepInstanceRepository.findByUid(stepId);
                if(foundStep != null && foundStep.getErrorMessage()  != null && !foundStep.getErrorMessage().isEmpty()){
                    errorMessages.put(foundStep.getStepName(), foundStep.getErrorMessage());
                }
            }
            workflowInstance.setErrorMessages(errorMessages);
            workflowInstanceRepository.save(workflowInstance);
            switch (stepInstance.getTaskStatus()){
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
        }
        else {
            instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.COMPLETED);
            instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.COMPLETED);
        }
    }



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

    private void runStep(Task task, WorkflowStepInstance firstStep) {
        queueController.addTaskToQueue(task);
        instanceController.updateWorkflowStepInstanceStatus(firstStep, TaskStatus.IN_PROGRESS);
    }

    public WorkflowExecution stop(String workflowExecutionId) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(workflowExecutionId);
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(execution.getWorkflowInstanceId());
        instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.STOPPED);
        instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.STOPPED);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    public WorkflowExecution pause(String workflowExecutionId) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(workflowExecutionId);
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(execution.getWorkflowInstanceId());
        instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.PAUSED);
        instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.PAUSED);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    public WorkflowExecution resume(String workflowExecutionId) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(workflowExecutionId);
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(execution.getWorkflowInstanceId());
        instanceController.updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.RUNNING);
        instanceController.updateWorkflowExecutionStatus(execution, WorkflowStatus.RUNNING);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    public WorkflowExecution executionState(String workflowExecutionId) {
        return workflowExecutionRepository.findByRunId(workflowExecutionId);
    }

    private WorkflowStepInstance getStartStep(WorkflowInstance workflow) {
        for (WorkflowStepInstance step : workflowStepInstanceRepository.findByWorkflowInstanceId(workflow.getUid())) {
            if (step.getWorkFlowStepType() == WorkFlowStepType.START) return step;
        }
        return null;
    }

    private Map<String, String> generateTaskMetaData(WorkflowExecution execution, WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put(Task.METADATA_KEY_EXECUTION_ID, execution.getRunId());
        metaData.put(Task.METADATA_KEY_WORKFlOW_INSTANCE_ID, workflowInstance.getUid());
        metaData.put(Task.METADATA_KEY_STEP_INSTANCE_ID, workflowStepInstance.getUid());
        return metaData;
    }

    private WorkflowExecution createWorkflowExecution(Workflow workflow, WorkflowInstance workflowInstance) {
        List<WorkflowStepInstance> stepInstances = new ArrayList<>();
        for (String setInstanceId : workflowInstance.getStepInstanceIds()) {
            stepInstances.add(workflowStepInstanceRepository.findByUid(setInstanceId));
        }
        WorkflowExecution workflowExecution = new WorkflowExecution(workflow, workflowInstance, stepInstances);
        return workflowExecutionRepository.save(workflowExecution);
    }



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
