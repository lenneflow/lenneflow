package de.lenneflow.orchestrationservice.utils;

import de.lenneflow.orchestrationservice.enums.RunStatus;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.model.WorkflowInstance;

public class ObjectMapper {

    private ObjectMapper(){}

    public static WorkflowInstance mapToWorkflowInstance(Workflow workflow){
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setName(workflow.getName());
        workflowInstance.setDescription(workflow.getDescription());
        workflowInstance.setWorkflowUid(workflow.getUid());
        workflowInstance.setRestartable(workflow.isRestartable());
        workflowInstance.setTimeOutInSeconds(workflow.getTimeOutInSeconds());
        return workflowInstance;
    }
}
