package de.lenneflow.orchestrationservice.feignclients;

import de.lenneflow.orchestrationservice.enums.TaskStatus;
import de.lenneflow.orchestrationservice.enums.WorkFlowStepType;
import de.lenneflow.orchestrationservice.enums.WorkflowStatus;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.feignmodels.WorkflowStep;

import java.util.ArrayList;
import java.util.List;

public class WorkflowServiceClientImpl {

    public Workflow getWorkflow(String uuid) {
        Workflow workflow = new Workflow();
        workflow.setUid("w1");
        workflow.setStatus(WorkflowStatus.NOT_RUN);
        workflow.setName("MyWorkflow");
        workflow.setRestartable(true);
        workflow.setDescription("Description");
        workflow.setVersion(1);
        workflow.setSteps(getWorkflowSteps("w1"));
        return workflow;
    }


    public WorkflowStep getWorkflowStep(String workflowId, String stepId) {
        List<WorkflowStep> workflowSteps = getWorkflowSteps(workflowId);
        for (WorkflowStep step : workflowSteps) {
            if (step.getUid().equals(stepId)) {
                return step;
            }
        }
        return null;
    }


    public List<WorkflowStep> getWorkflowSteps(String workflowId) {
        List<WorkflowStep> workflowSteps = new ArrayList<>();
        for(int i=0; i<5; i++){
            WorkflowStep step = new WorkflowStep();
            step.setUid("s"+i);
            step.setDescription("Description");
            step.setStatus(TaskStatus.NOT_RUN);
            step.setTaskId("t"+i);
            step.setWorkflowId("w1");
            step.setWorkFlowStepType(WorkFlowStepType.SIMPLE);
            if(i == 0){
                step.setWorkFlowStepType(WorkFlowStepType.START);
            }
            if(i == 4){
                step.setWorkFlowStepType(WorkFlowStepType.TERMINATE);
            }
            workflowSteps.add(step);
        }
        for(int i=0; i<5; i++){
            WorkflowStep step = workflowSteps.get(i);
            if(i == 0){
                step.setNextStepId(workflowSteps.get(i+1).getUid());
            }
            if(i == 4){
                step.setPreviousStepId(workflowSteps.get(i-1).getUid());
            }
            if(i > 0 && i < 4){
                step.setNextStepId(workflowSteps.get(i+1).getUid());
                step.setPreviousStepId(workflowSteps.get(i-1).getUid());
            }
        }
        return workflowSteps;
    }
}
