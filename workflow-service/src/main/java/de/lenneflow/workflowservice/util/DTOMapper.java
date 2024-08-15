package de.lenneflow.workflowservice.util;

import de.lenneflow.workflowservice.dto.SimpleWorkflowStep;
import de.lenneflow.workflowservice.dto.SwitchWorkflowStep;
import de.lenneflow.workflowservice.enums.WorkFlowStepType;
import de.lenneflow.workflowservice.model.WorkflowStep;
import org.modelmapper.ModelMapper;

public class DTOMapper {

    static ModelMapper modelMapper = new ModelMapper();

    public static WorkflowStep fromSimpleStep(SimpleWorkflowStep simpleWorkflowStep) {
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setUid(simpleWorkflowStep.getUid());
        workflowStep.setWorkflowUid(simpleWorkflowStep.getWorkflowUid());
        workflowStep.setDescription(simpleWorkflowStep.getDescription());
        workflowStep.setStepName(simpleWorkflowStep.getStepName());
        workflowStep.setFunctionId(simpleWorkflowStep.getFunctionId());
        workflowStep.setExecutionOrder(simpleWorkflowStep.getExecutionOrder());
        workflowStep.setInputData(simpleWorkflowStep.getInputData());
        workflowStep.setRetryCount(simpleWorkflowStep.getRetryCount());
        workflowStep.setWorkFlowStepType(WorkFlowStepType.SIMPLE);
        workflowStep.setCreationTime(simpleWorkflowStep.getCreationTime());
        workflowStep.setUpdateTime(simpleWorkflowStep.getUpdateTime());
        return workflowStep;
    }
    public static SimpleWorkflowStep toSimpleStep(WorkflowStep workflowStep) {
        SimpleWorkflowStep simpleWorkflowStep = new SimpleWorkflowStep();
        simpleWorkflowStep.setUid(workflowStep.getUid());
        simpleWorkflowStep.setWorkflowUid(workflowStep.getWorkflowUid());
        simpleWorkflowStep.setDescription(workflowStep.getDescription());
        simpleWorkflowStep.setStepName(workflowStep.getStepName());
        simpleWorkflowStep.setFunctionId(workflowStep.getFunctionId());
        simpleWorkflowStep.setExecutionOrder(workflowStep.getExecutionOrder());
        simpleWorkflowStep.setInputData(workflowStep.getInputData());
        simpleWorkflowStep.setRetryCount(workflowStep.getRetryCount());
        simpleWorkflowStep.setCreationTime(workflowStep.getCreationTime());
        simpleWorkflowStep.setUpdateTime(workflowStep.getUpdateTime());
        return simpleWorkflowStep;
    }

    public static WorkflowStep fromSwitchStep(SwitchWorkflowStep switchWorkflowStep) {
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setUid(switchWorkflowStep.getUid());
        workflowStep.setWorkflowUid(switchWorkflowStep.getWorkflowUid());
        workflowStep.setDescription(switchWorkflowStep.getDescription());
        workflowStep.setStepName(switchWorkflowStep.getStepName());
        workflowStep.setSwitchCondition(switchWorkflowStep.getSwitchCondition());
        workflowStep.setExecutionOrder(switchWorkflowStep.getExecutionOrder());
        workflowStep.setInputData(switchWorkflowStep.getInputData());
        workflowStep.setDecisionCases(switchWorkflowStep.getDecisionCases());
        workflowStep.setWorkFlowStepType(WorkFlowStepType.SWITCH);
        workflowStep.setCreationTime(switchWorkflowStep.getCreationTime());
        workflowStep.setUpdateTime(switchWorkflowStep.getUpdateTime());
        return workflowStep;
    }

    public static SwitchWorkflowStep toSwitchStep(WorkflowStep workflowStep) {
        return modelMapper.map(workflowStep, SwitchWorkflowStep.class);
    }

}

