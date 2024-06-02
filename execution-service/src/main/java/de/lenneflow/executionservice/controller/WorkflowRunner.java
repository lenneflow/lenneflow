package de.lenneflow.executionservice.controller;

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
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.*;

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
        WorkflowExecution execution = workflowExecutionRepository.findByUid(taskResult.getMetaData().get(Task.METADATA_KEY_EXECUTION_ID));
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(taskResult.getMetaData().get(Task.METADATA_KEY_WORKFlOW_INSTANCE_ID));
        WorkflowStepInstance stepInstance = workflowStepInstanceRepository.findByUid(taskResult.getMetaData().get(Task.METADATA_KEY_STEP_INSTANCE_ID));
        updateWorkflowStepInstanceStatus(stepInstance, taskResult);
        updateWorkflowStepInstanceOutput(stepInstance, taskResult);
        switch (taskResult.getTaskStatus()) {
            case COMPLETED, SKIPPED:
                WorkflowStepInstance nextStep = getNextStep(stepInstance);
                if (nextStep != null) {
                    Task task =  taskServiceClient.getTask(nextStep.getTaskId());
                    Map<String, String> metadata = generateTaskMetaData(execution, workflowInstance, stepInstance);
                    task.setMetaData(metadata);
                    queueUtil.addTaskToQueue(task);
                } else {
                    workflowInstance.setStatus(WorkflowStatus.COMPLETED);
                }
                break;


            default:
                throw new IllegalStateException("Unexpected value: " + taskResult.getTaskStatus());
        }

    }

    private WorkflowStepInstance getNextStep(WorkflowStepInstance stepInstance) {
        if(stepInstance.isEnd()){return null;}
        switch (stepInstance.getWorkFlowStepType()){
            case SIMPLE:
                return stepInstance.getNextStep();
            case DO_WHILE:
                if(checkDoWhileStopCriteria(stepInstance))
                    return stepInstance;
                else
                    return  getNextStep(stepInstance);
            case SWITCH:
                return stepInstance.getDecisionCases().get(getSwitchCase(stepInstance)).get(0);
            default:
                return null;
        }

    }

    private String getSwitchCase(WorkflowStepInstance stepInstance) {
        return stepInstance.getInputData().values().iterator().next().toString();
    }

    private boolean checkDoWhileStopCriteria(WorkflowStepInstance stepInstance) {
        return false;
    }

    public WorkflowInstance start(String workflowId) {
        WorkflowInstance workflowInstance = createWorkflowInstance(workflowId);
        updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.RUNNING);
        WorkflowExecution execution = createWorkflowExecution(workflowInstance);

        WorkflowStepInstance firstStep = getStartStep(workflowInstance);
        assert firstStep != null;
        Task task = taskServiceClient.getTask(firstStep.getTaskId());
        Map<String, String> metadata = generateTaskMetaData(execution, workflowInstance, firstStep);
        task.setMetaData(metadata);
        queueUtil.addTaskToQueue(task);
        return workflowInstance;
    }

    public WorkflowInstance stop(String workflowInstanceId) {
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(workflowInstanceId);
        updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.STOPPED);
        return workflowInstance;
    }

    public WorkflowInstance pause(String workflowInstanceId) {
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(workflowInstanceId);
        updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.PAUSED);
        return workflowInstance;
    }

    public WorkflowInstance resume(String workflowInstanceId) {
        WorkflowInstance workflowInstance = workflowInstanceRepository.findByUid(workflowInstanceId);
        updateWorkflowInstanceStatus(workflowInstance, WorkflowStatus.RUNNING);
        return workflowInstance;
    }

    private WorkflowStepInstance getStartStep(WorkflowInstance workflow) {
        for (WorkflowStepInstance step : workflowStepInstanceRepository.findByWorkflowInstanceId(workflow.getUid())) {
            if (step.isStart()) return step;
        }
        return null;
    }

    private Map<String, String> generateTaskMetaData(WorkflowExecution execution, WorkflowInstance workflowInstance, WorkflowStepInstance workflowStepInstance) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put(Task.METADATA_KEY_EXECUTION_ID, execution.getUid());
        metaData.put(Task.METADATA_KEY_WORKFlOW_INSTANCE_ID, workflowInstance.getUid());
        metaData.put(Task.METADATA_KEY_STEP_INSTANCE_ID, workflowStepInstance.getUid());
        return metaData;
    }

    private WorkflowExecution createWorkflowExecution(WorkflowInstance workflowInstance) {
        WorkflowExecution workflowExecution = new WorkflowExecution();
        workflowExecution.setUid(UUID.randomUUID().toString());
        workflowExecution.setWorkflowDescription(workflowInstance.getDescription());
        workflowExecution.setWorkflowName(workflowInstance.getName());
        workflowExecution.setWorkflowStatus(workflowInstance.getStatus());
        return workflowExecutionRepository.save(workflowExecution);
    }

    private WorkflowInstance createWorkflowInstance(String workflowId) {
        Workflow workflow = workflowServiceClient.getWorkflow(workflowId);
        WorkflowInstance workflowInstance = new WorkflowInstance(workflow);
        workflowInstance.setStatus(WorkflowStatus.NOT_RUN);
        List<WorkflowStep> steps = workflowServiceClient.getWorkflowSteps(workflowId);

        for (WorkflowStep step : steps) {
            WorkflowStepInstance stepInstance = new WorkflowStepInstance(step, workflowInstance.getUid());
            workflowStepInstanceRepository.save(stepInstance);
        }
        List<WorkflowStepInstance> stepInstances = updateWorkflowSteps(steps);
        workflowInstance.setStepInstances(stepInstances);
        return workflowInstanceRepository.save(workflowInstance);
    }

    private List<WorkflowStepInstance> updateWorkflowSteps(List<WorkflowStep> steps){
        List<WorkflowStepInstance> stepInstances = new ArrayList<>();
        for (WorkflowStep step : steps) {
            WorkflowStepInstance stepInstance = workflowStepInstanceRepository.findByWorkflowStepId(step.getId());
            WorkflowStep nextStep = step.getNextStep();
            if(nextStep != null) {
                WorkflowStepInstance nextStepInstance = workflowStepInstanceRepository.findByWorkflowStepId(nextStep.getId());
                stepInstance.setNextStep(nextStepInstance);
            }
            WorkflowStep previousStep = step.getPreviousStep();
            if(previousStep != null) {
                WorkflowStepInstance previousStepInstance = workflowStepInstanceRepository.findByWorkflowStepId(previousStep.getId());
                stepInstance.setPreviousStep(previousStepInstance);
            }
            Map<String, List<WorkflowStep>> decisionCases = step.getDecisionCases();
            if(decisionCases != null && !decisionCases.isEmpty()) {
                Map<String, List<WorkflowStepInstance>> decisionCaseInstances = new HashMap<>();
                for(Map.Entry<String, List<WorkflowStep>> entry : decisionCases.entrySet()) {
                    List<WorkflowStepInstance> instances = new LinkedList<>();
                    for(WorkflowStep decisionCase : entry.getValue()) {
                        WorkflowStepInstance decisionCaseInstance = workflowStepInstanceRepository.findByWorkflowStepId(decisionCase.getId());
                        instances.add(decisionCaseInstance);
                    }
                    decisionCaseInstances.put(entry.getKey(), instances);
                }
                stepInstance.setDecisionCases(decisionCaseInstances);
            }
            workflowStepInstanceRepository.save(stepInstance);
            stepInstances.add(stepInstance);
        }
        return stepInstances;
    }

    private void updateWorkflowInstanceStatus(WorkflowInstance workflowInstance, WorkflowStatus workflowStatus) {
        workflowInstance.setStatus(workflowStatus);
        workflowInstanceRepository.save(workflowInstance);
    }

    private void updateWorkflowStepInstanceStatus(WorkflowStepInstance workflowStepInstance, TaskResult taskResult) {
        TaskStatus taskStatus = taskResult.getTaskStatus();
        workflowStepInstance.setTaskStatus(taskStatus);
        workflowStepInstanceRepository.save(workflowStepInstance);
    }

    private void updateWorkflowStepInstanceOutput(WorkflowStepInstance workflowStepInstance, TaskResult taskResult) {
        Map<String, Object> output = taskResult.getOutputData();
        workflowStepInstance.setOutputData(output);
        workflowStepInstanceRepository.save(workflowStepInstance);
    }

}
