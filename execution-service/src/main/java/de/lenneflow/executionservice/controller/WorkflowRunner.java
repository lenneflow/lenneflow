package de.lenneflow.executionservice.controller;

import de.lenneflow.executionservice.enums.TaskStatus;
import de.lenneflow.executionservice.enums.WorkflowStatus;
import de.lenneflow.executionservice.feignclients.TaskServiceClient;
import de.lenneflow.executionservice.feignclients.WorkflowServiceClient;
import de.lenneflow.executionservice.feignmodels.Task;
import de.lenneflow.executionservice.feignmodels.TaskResult;
import de.lenneflow.executionservice.model.WorkflowExecution;
import de.lenneflow.executionservice.model.WorkflowInstance;
import de.lenneflow.executionservice.model.WorkflowStepInstance;
import de.lenneflow.executionservice.queue.QueueUtil;
import de.lenneflow.executionservice.repository.WorkflowExecutionRepository;
import de.lenneflow.executionservice.repository.WorkflowInstanceRepository;
import de.lenneflow.executionservice.repository.WorkflowStepInstanceRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class WorkflowRunner {

    private static final String TASKRESULTQUEUE = "TaskResultQueue";


    final TaskServiceClient taskServiceClient;
    final WorkflowServiceClient workflowServiceClient;
    final WorkflowExecutionRepository workflowExecutionRepository;
    final WorkflowInstanceRepository workflowInstanceRepository;
    final WorkflowStepInstanceRepository workflowStepInstanceRepository;
    final QueueUtil queueUtil;

    WorkflowRunner(TaskServiceClient taskServiceClient, WorkflowServiceClient workflowServiceClient, WorkflowExecutionRepository workflowExecutionRepository, WorkflowInstanceRepository workflowInstanceRepository, WorkflowStepInstanceRepository workflowStepInstanceRepository, QueueUtil queueUtil) {
        this.taskServiceClient = taskServiceClient;
        this.workflowServiceClient = workflowServiceClient;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
        this.queueUtil = queueUtil;
    }

    @RabbitListener(queues = TASKRESULTQUEUE)
    public void processTasksResult(TaskResult taskResult) {
        WorkflowExecution execution = workflowExecutionRepository.findByExecutionID(taskResult.getMetaData().get(Task.METADATA_KEY_EXECUTION_ID));
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByInstanceID(taskResult.getMetaData().get(Task.METADATA_KEY_WORKFlOW_INSTANCE_ID));
        WorkflowStepInstance stepInstance = workflowStepInstanceRepository.findByInstanceId(taskResult.getMetaData().get(Task.METADATA_KEY_STEP_INSTANCE_ID));
        updateWorkflowStepInstanceStatus(stepInstance, taskResult.getTaskStatus());
        switch (taskResult.getTaskStatus()) {
            case COMPLETED, SKIPPED:

                if (!stepInstance.isEnd()) {
                    WorkflowStepInstance nextStep = stepInstance.getNextStep();
                    Task task = nextStep.getTask();
                    Map<String, String> metadata = generateTaskMetaData(execution, workflowInstance, stepInstance);
                    task.setMetaData(metadata);
                    queueUtil.addTaskToQueue(nextStep.getTask());
                } else {
                    workflowInstance.setStatus(WorkflowStatus.COMPLETED);
                }
                break;
            case SKIPPED:
                updateWorkflowStepInstanceStatus(stepInstance, WorkflowStepStatus.SK);

            default:
                throw new IllegalStateException("Unexpected value: " + taskResult.getTaskStatus());
        }

    }

    private WorkflowStepInstance getNextStep(WorkflowStepInstance stepInstance) {
        switch (stepInstance.getStepType()){
            case SIMPLE:
                return stepInstance.getNextStep();
        }

    }

    public WorkflowInstance start(String workflowId) {
        WorkflowInstance workflowInstance = createWorkflowInstance(workflowId);
        updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.RUNNING);
        WorkflowExecution execution = createWorkflowExecution(workflowInstance);

        WorkflowStepInstance initial = getInitialStep(workflowInstance);
        assert initial != null;
        Task task = initial.getTask();
        Map<String, String> metadata = generateTaskMetaData(execution, workflowInstance, initial);
        task.setMetaData(metadata);
        queueUtil.addTaskToQueue(task);
        return workflowInstance;
    }

    public WorkflowInstance stop(String workflowInstanceId) {
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByInstanceID(workflowInstanceId);
        updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.STOPPED);
        return workflowInstance;
    }

    public WorkflowInstance pause(String workflowInstanceId) {
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByInstanceID(workflowInstanceId);
        updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.PAUSED);
        return workflowInstance;
    }

    public WorkflowInstance resume(String workflowInstanceId) {
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByInstanceID(workflowInstanceId);
        updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.RUNNING);
        return workflowInstance;
    }

    private WorkflowStepInstance getInitialStep(WorkflowInstance workflow) {
        for (WorkflowStepInstance step : workflowStepInstanceRepository.findByWorkflowInstanceId(workflow.getInstanceID())) {
            if (step.isStart()) return step;
        }
        return null;
    }

    private Map<String, String> generateTaskMetaData(WorkflowExecution execution, WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put(Task.METADATA_KEY_EXECUTION_ID, execution.getExecutionId());
        metaData.put(Task.METADATA_KEY_WORKFlOW_INSTANCE_ID, workflowInstance.getInstanceID());
        metaData.put(Task.METADATA_KEY_STEP_INSTANCE_ID, workflowStepInstance.getInstanceId());
        return metaData;
    }

    private WorkflowExecution createWorkflowExecution(WorkflowInstance workflowInstance) {
        WorkflowExecution workflowExecution = new WorkflowExecution();
        workflowExecution.setExecutionId(UUID.randomUUID().toString());
        workflowExecution.setWorkflowDescription(workflowInstance.getDescription());
        workflowExecution.setWorkflowName(workflowInstance.getName());
        workflowExecution.setWorkflowStatus(workflowInstance.getStatus());
        return workflowExecutionRepository.save(workflowExecution);
    }

    private WorkflowInstance createWorkflowInstance(String workflowId) {
        WorkflowInstance workflowInstance = workflowServiceClient.getWorkflow(workflowId);
        workflowInstance.setInstanceID(UUID.randomUUID().toString());
        List<WorkflowStepInstance> stepInstances = workflowServiceClient.getWorkflowSteps(workflowId);
        for (WorkflowStepInstance instance : stepInstances) {
            instance.setInstanceId(UUID.randomUUID().toString());
            instance.setWorkflowInstanceId(workflowInstance.getInstanceID());
            workflowStepInstanceRepository.save(instance);
        }
        //workflowInstance.setStepInstances(workflowStepInstanceRepository.findByWorkflowInstanceId(workflowInstance.getInstanceID()));
        return workflowInstanceRepository.save(workflowInstance);
    }

    private void updateWorkflowInstanceStatus(WorkflowInstance workflowInstance, WorkflowStatus workflowStatus) {
        workflowInstance.setStatus(workflowStatus);
        workflowInstanceRepository.save(workflowInstance);
    }

    private void updateWorkflowStepInstanceStatus(WorkflowStepInstance workflowStepInstance, TaskStatus taskStatus) {
        workflowStepInstance.setTaskStatus(taskStatus);
        workflowStepInstanceRepository.save(workflowStepInstance);
    }
}
