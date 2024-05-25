package de.lenneflow.executionservice.utils;

import de.lenneflow.executionservice.enums.StepTaskType;
import de.lenneflow.executionservice.feignclients.TaskServiceClient;
import de.lenneflow.executionservice.feignmodels.Workflow;
import de.lenneflow.executionservice.feignmodels.WorkflowStep;
import de.lenneflow.executionservice.model.WorkflowExecution;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class WorkflowRunner {

    private static final String WORKERTASKRESULTQUEUE = "WorkerTaskResultQueue";
    private static final String SYSTEMTASKRESULTQUEUE = "SystemTaskResultQueue";


    final TaskServiceClient taskServiceClient;

    WorkflowRunner(TaskServiceClient taskServiceClient) {
        this.taskServiceClient = taskServiceClient;
    }

    @RabbitListener(queues = WORKERTASKRESULTQUEUE)
    public void processWorkerTasksResult(String orderId) {
        // Process payment logic for the order
        System.out.println("Payment processed for order: " + orderId);
    }

    public void start(WorkflowExecution execution, Workflow workflow){
        WorkflowStep initial = getInitialStep(workflow);
        assert initial != null;
        runStep(initial);
    }

    public void runStep(WorkflowStep step){
        StepTaskType stepType = step.getStepTaskType();
        if(stepType == StepTaskType.SYSTEM){
            taskServiceClient.enqueueSystemTask(step.getUuid(), step.getSystemTaskId());
        }else{
            taskServiceClient.enqueueWorkerTask(step.getUuid(), step.getWorkerTaskId());
        }
    }


    private WorkflowStep getInitialStep(Workflow workflow){
        for(WorkflowStep step : workflow.getSteps()){
            if(step.isStart()) return step;
        }
        return null;
    }
}
