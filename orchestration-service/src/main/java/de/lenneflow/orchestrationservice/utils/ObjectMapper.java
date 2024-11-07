package de.lenneflow.orchestrationservice.utils;

import de.lenneflow.orchestrationservice.dto.GlobalInputDataDto;
import de.lenneflow.orchestrationservice.dto.QueueElement;
import de.lenneflow.orchestrationservice.feignmodels.Function;
import de.lenneflow.orchestrationservice.feignmodels.Workflow;
import de.lenneflow.orchestrationservice.model.GlobalInputData;
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

    public static GlobalInputData mapToGlobalInputData(GlobalInputDataDto globalInputDataDto){
        GlobalInputData globalInputData = new GlobalInputData();
        globalInputData.setName(globalInputDataDto.getName());
        globalInputData.setDescription(globalInputDataDto.getDescription());
        globalInputData.setInputData(globalInputDataDto.getInputData());
        return globalInputData;
    }



    /**
     * Maps a function to a function dto object
     *
     * @param function the function to map
     * @return the {@link QueueElement} object
     */
    public static QueueElement mapFunctionQueueElement(Function function) {
        QueueElement queueElement = new QueueElement();
        queueElement.setFunctionName(function.getName());
        queueElement.setFunctionType(queueElement.getFunctionType());
        queueElement.setCpuRequest(function.getCpuRequest());
        queueElement.setMemoryRequest(function.getMemoryRequest());
        queueElement.setServiceUrl(function.getServiceUrl());
        return queueElement;
    }
}
