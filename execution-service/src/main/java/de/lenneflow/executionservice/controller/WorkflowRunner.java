package de.lenneflow.executionservice.controller;

import de.lenneflow.executionservice.ExecutionServiceApplication;
import de.lenneflow.executionservice.enums.TaskStatus;
import de.lenneflow.executionservice.enums.WorkflowStatus;
import de.lenneflow.executionservice.feignclients.TaskServiceClient;
import de.lenneflow.executionservice.feignclients.WorkflowServiceClient;
import de.lenneflow.executionservice.feignmodels.Task;
import de.lenneflow.executionservice.feignmodels.TaskResult;
import de.lenneflow.executionservice.feignmodels.Workflow;
import de.lenneflow.executionservice.feignmodels.WorkflowStep;
import de.lenneflow.executionservice.model.WorkflowExecution;
import de.lenneflow.executionservice.model.WorkflowInstance;
import de.lenneflow.executionservice.model.WorkflowStepInstance;
import de.lenneflow.executionservice.queue.QueueUtil;
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
    final QueueUtil queueUtil;

    WorkflowRunner(TaskServiceClient taskServiceClient, WorkflowServiceClient workflowServiceClient, WorkflowExecutionRepository workflowExecutionRepository, WorkflowInstanceRepository workflowInstanceRepository, WorkflowStepInstanceRepository workflowStepInstanceRepository, QueueUtil queueUtil) {
        this.taskServiceClient = taskServiceClient;
        this.workflowServiceClient = workflowServiceClient;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowStepInstanceRepository = workflowStepInstanceRepository;
        this.queueUtil = queueUtil;
    }

    @RabbitListener(queues = ExecutionServiceApplication.TASKRESULTQUEUE)
    public void processTasksResult(byte[] serializedTaskResult) {
        TaskResult taskResult = Util.deserializeTaskResult(serializedTaskResult);
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(taskResult.getMetaData().get(Task.METADATA_KEY_EXECUTION_ID));
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(taskResult.getMetaData().get(Task.METADATA_KEY_WORKFlOW_INSTANCE_ID));
        WorkflowStepInstance stepInstance = workflowStepInstanceRepository.findByUid(taskResult.getMetaData().get(Task.METADATA_KEY_STEP_INSTANCE_ID));
        updateCurrentStep(stepInstance, taskResult);
        switch (taskResult.getTaskStatus()) {
            case COMPLETED, SKIPPED:
                WorkflowStepInstance nextStepInstance = getNextStep(stepInstance);
                if (nextStepInstance != null) {
                    Task task =  taskServiceClient.getTask(nextStepInstance.getTaskId());
                    Map<String, String> metadata = generateTaskMetaData(execution, workflowInstance, nextStepInstance);
                    task.setMetaData(metadata);
                    runStep(task, nextStepInstance);
                } else {
                    updateWorkflowInstanceAndExecutionStatus(workflowInstance, execution, WorkflowStatus.COMPLETED);
                }
                break;


            default:
                throw new IllegalStateException("Unexpected value: " + taskResult.getTaskStatus());
        }

    }

    private void updateCurrentStep(WorkflowStepInstance stepInstance, TaskResult taskResult) {
        updateWorkflowStepInstanceStatus(stepInstance, taskResult.getTaskStatus());
        updateWorkflowStepInstanceOutput(stepInstance, taskResult);
    }

    public WorkflowExecution start(String workflowId) {
        WorkflowInstance workflowInstance = createWorkflowInstance(workflowId);
        Workflow workflow = workflowServiceClient.getWorkflow(workflowId);
        WorkflowExecution execution = createWorkflowExecution(workflow, workflowInstance);
        updateWorkflowInstanceAndExecutionStatus(workflowInstance,execution, WorkflowStatus.RUNNING);

        WorkflowStepInstance firstStep = getStartStep(workflowInstance);
        assert firstStep != null;
        Task task = taskServiceClient.getTask(firstStep.getTaskId());
        Map<String, String> metadata = generateTaskMetaData(execution, workflowInstance, firstStep);
        task.setMetaData(metadata);
        runStep(task, firstStep);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    private void runStep(Task task, WorkflowStepInstance firstStep) {
        queueUtil.addTaskToQueue(task);
        updateWorkflowStepInstanceStatus(firstStep, TaskStatus.IN_PROGRESS);
    }

    public WorkflowExecution stop(String workflowExecutionId) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(workflowExecutionId);
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(execution.getWorkflowInstanceId());
        updateWorkflowInstanceAndExecutionStatus(workflowInstance,execution, WorkflowStatus.STOPPED);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    public WorkflowExecution pause(String workflowExecutionId) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(workflowExecutionId);
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(execution.getWorkflowInstanceId());
        updateWorkflowInstanceAndExecutionStatus(workflowInstance,execution, WorkflowStatus.PAUSED);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    public WorkflowExecution resume(String workflowExecutionId) {
        WorkflowExecution execution = workflowExecutionRepository.findByRunId(workflowExecutionId);
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(execution.getWorkflowInstanceId());
        updateWorkflowInstanceAndExecutionStatus(workflowInstance,execution, WorkflowStatus.RUNNING);
        return workflowExecutionRepository.findByRunId(execution.getRunId());
    }

    public WorkflowExecution runState(String workflowExecutionId) {
        return workflowExecutionRepository.findByRunId(workflowExecutionId);
    }

    private WorkflowStepInstance getStartStep(WorkflowInstance workflow) {
        for (WorkflowStepInstance step : workflowStepInstanceRepository.findByWorkflowInstanceId(workflow.getUid())) {
            if (step.isStart()) return step;
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
        for(String setInstanceId : workflowInstance.getStepInstanceIds()) {
          stepInstances.add(workflowStepInstanceRepository.findByUid(setInstanceId));
        }
        WorkflowExecution workflowExecution = new WorkflowExecution(workflow, workflowInstance, stepInstances);
        return workflowExecutionRepository.save(workflowExecution);
    }

    private String getSwitchCase(WorkflowStepInstance stepInstance) {
        return stepInstance.getInputData().values().iterator().next().toString();
    }

    private boolean checkDoWhileStopCriteria(WorkflowStepInstance stepInstance) {
        return (boolean) stepInstance.getInputData().values().iterator().next();
    }

    private WorkflowInstance createWorkflowInstance(String workflowId) {
        Workflow workflow = workflowServiceClient.getWorkflow(workflowId);
        WorkflowInstance workflowInstance = new WorkflowInstance(workflow);
        workflowInstanceRepository.save(workflowInstance);
        workflowInstance.setStatus(WorkflowStatus.NOT_RUN);
        List<WorkflowStep> steps = workflowServiceClient.getWorkflowSteps(workflowId);
        Map<String, String>  stepStepInstanceMapping = new HashMap<>();

        for (WorkflowStep step : steps) {
            WorkflowStepInstance stepInstance = new WorkflowStepInstance(step, workflowInstance.getUid());
            stepStepInstanceMapping.put(step.getUid(), stepInstance.getUid());
            workflowStepInstanceRepository.save(stepInstance);
        }
        List<String>  stepInstanceIds = updateWorkflowStepInstances(workflowId, steps, stepStepInstanceMapping);
        workflowInstance.setStepInstanceIds(stepInstanceIds);
        return workflowInstanceRepository.save(workflowInstance);
    }

    private WorkflowStepInstance getNextStep(WorkflowStepInstance stepInstance) {
        if(stepInstance.isEnd()){return null;}
        switch (stepInstance.getWorkFlowStepType()){
            case SIMPLE:
                return  workflowStepInstanceRepository.findByUid(stepInstance.getNextStepId());
            case DO_WHILE:
                if(checkDoWhileStopCriteria(stepInstance))
                    return stepInstance;
                else
                    return  getNextStep(stepInstance);
            case SWITCH:
                String stepInstanceId =  stepInstance.getDecisionCases().get(getSwitchCase(stepInstance));
                return  workflowStepInstanceRepository.findByUid(stepInstanceId);
            default:
                return null;
        }

    }

    private List<String> updateWorkflowStepInstances(String workflowId, List<WorkflowStep> steps, Map<String, String>  stepStepInstanceMapping){
        List<String> stepInstanceIds = new ArrayList<>();
        for (WorkflowStep step : steps) {
            WorkflowStepInstance stepInstance = workflowStepInstanceRepository.findByUid(stepStepInstanceMapping.get(step.getUid()));
            WorkflowStep nextStep =   workflowServiceClient.getWorkflowStep(workflowId, step.getNextStepId()) ;
            if(nextStep != null) {
                WorkflowStepInstance nextStepInstance = workflowStepInstanceRepository.findByUid(stepStepInstanceMapping.get(step.getNextStepId()));
                stepInstance.setNextStepId(nextStepInstance.getUid());
            }
            WorkflowStep previousStep = workflowServiceClient.getWorkflowStep(workflowId, step.getPreviousStepId()) ;
            if(previousStep != null) {
                WorkflowStepInstance previousStepInstance = workflowStepInstanceRepository.findByUid(stepStepInstanceMapping.get(step.getPreviousStepId()));
                stepInstance.setPreviousStepId(previousStepInstance.getUid());
            }
            Map<String, String> decisionCases = step.getDecisionCases();
            if(decisionCases != null && !decisionCases.isEmpty()) {
                Map<String, String> decisionCaseInstances = new HashMap<>();
                for(Map.Entry<String, String> entry : decisionCases.entrySet()) {
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

    private void updateWorkflowInstanceAndExecutionStatus(WorkflowInstance workflowInstance, WorkflowExecution execution, WorkflowStatus workflowStatus) {
        workflowInstance.setStatus(workflowStatus);
        execution.setWorkflowStatus(workflowStatus);
        workflowInstanceRepository.save(workflowInstance);
        workflowExecutionRepository.save(execution);
    }

    private WorkflowStepInstance updateWorkflowStepInstanceStatus(WorkflowStepInstance workflowStepInstance, TaskStatus taskStatus) {
        workflowStepInstance.setTaskStatus(taskStatus);
        return workflowStepInstanceRepository.save(workflowStepInstance);
    }

    private WorkflowStepInstance updateWorkflowStepInstanceOutput(WorkflowStepInstance workflowStepInstance, TaskResult taskResult) {
        Map<String, Object> output = taskResult.getOutputData();
        workflowStepInstance.setOutputData(output);
        return workflowStepInstanceRepository.save(workflowStepInstance);
    }

}
