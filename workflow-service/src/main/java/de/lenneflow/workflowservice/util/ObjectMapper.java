package de.lenneflow.workflowservice.util;

import de.lenneflow.workflowservice.dto.*;
import de.lenneflow.workflowservice.enums.ControlStructure;
import de.lenneflow.workflowservice.model.JsonSchema;
import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.model.WorkflowStep;

public class ObjectMapper {

    private ObjectMapper(){
    }


    public static Workflow mapToWorkflow(WorkflowDTO workflowDTO) {
        if (workflowDTO == null) {
            return null;
        }
        Workflow workflow = new Workflow();
        workflow.setDescription(workflowDTO.getDescription());
        workflow.setName(workflowDTO.getName());
        workflow.setTimeOutInSeconds(Long.MAX_VALUE);
        return workflow;
    }

    public static WorkflowStep mapToWorkflowStep(WorkflowStep workflowStep, SubWorkflowStep subWorkflowStep) {
        if(subWorkflowStep == null){
            return null;
        }
        workflowStep.setName(subWorkflowStep.getName());
        workflowStep.setWorkflowUid(subWorkflowStep.getWorkflowUid());
        workflowStep.setSubWorkflowUid(subWorkflowStep.getSubWorkflowUid());
        workflowStep.setDescription(subWorkflowStep.getDescription());
        workflowStep.setControlStructure(ControlStructure.SUB_WORKFLOW);
        workflowStep.setExecutionOrder(subWorkflowStep.getExecutionOrder());
        workflowStep.setInputData(subWorkflowStep.getInputData());
        workflowStep.setRetryCount(subWorkflowStep.getRetryCount());
        return workflowStep;
    }
    public static WorkflowStep mapToWorkflowStep(SubWorkflowStep subWorkflowStep) {
        return mapToWorkflowStep(new WorkflowStep(), subWorkflowStep);
    }

    public static WorkflowStep mapToWorkflowStep(WhileWorkflowStep whileWorkflowStep) {
        return mapToWorkflowStep(new WorkflowStep(), whileWorkflowStep);
    }

    public static WorkflowStep mapToWorkflowStep(WorkflowStep workflowStep, WhileWorkflowStep whileWorkflowStep) {
        if(whileWorkflowStep == null){
            return null;
        }
        workflowStep.setName(whileWorkflowStep.getName());
        workflowStep.setWorkflowUid(whileWorkflowStep.getWorkflowUid());
        workflowStep.setDescription(whileWorkflowStep.getDescription());
        workflowStep.setControlStructure(ControlStructure.DO_WHILE);
        workflowStep.setExecutionOrder(whileWorkflowStep.getExecutionOrder());
        workflowStep.setFunctionUid(whileWorkflowStep.getFunctionUid());
        workflowStep.setStopCondition(whileWorkflowStep.getStopCondition());
        workflowStep.setInputData(whileWorkflowStep.getInputData());
        workflowStep.setRetryCount(whileWorkflowStep.getRetryCount());
        return workflowStep;

    }

    public static WorkflowStep mapToWorkflowStep(SwitchWorkflowStep switchWorkflowStep) {
        return mapToWorkflowStep(new WorkflowStep(), switchWorkflowStep);
    }

    public static WorkflowStep mapToWorkflowStep(WorkflowStep workflowStep, SwitchWorkflowStep switchWorkflowStep) {
        if(switchWorkflowStep == null){
            return null;
        }
        workflowStep.setName(switchWorkflowStep.getName());
        workflowStep.setWorkflowUid(switchWorkflowStep.getWorkflowUid());
        workflowStep.setDescription(switchWorkflowStep.getDescription());
        workflowStep.setRetryCount(switchWorkflowStep.getRetryCount());
        workflowStep.setControlStructure(ControlStructure.SWITCH);
        workflowStep.setExecutionOrder(switchWorkflowStep.getExecutionOrder());
        workflowStep.setSwitchCase(switchWorkflowStep.getSwitchCase());
        workflowStep.setDecisionCases(switchWorkflowStep.getDecisionCases());
        workflowStep.setInputData(switchWorkflowStep.getInputData());
        return workflowStep;

    }

    public static WorkflowStep mapToWorkflowStep(SimpleWorkflowStep simpleWorkflowStep) {
        return mapToWorkflowStep(new WorkflowStep(), simpleWorkflowStep);
    }

    public static WorkflowStep mapToWorkflowStep(WorkflowStep workflowStep, SimpleWorkflowStep simpleWorkflowStep) {
        if(simpleWorkflowStep == null){
            return null;
        }
        workflowStep.setName(simpleWorkflowStep.getName());
        workflowStep.setWorkflowUid(simpleWorkflowStep.getWorkflowUid());
        workflowStep.setDescription(simpleWorkflowStep.getDescription());
        workflowStep.setExecutionOrder(simpleWorkflowStep.getExecutionOrder());
        workflowStep.setControlStructure(ControlStructure.SIMPLE);
        workflowStep.setRetryCount(simpleWorkflowStep.getRetryCount());
        workflowStep.setFunctionUid(simpleWorkflowStep.getFunctionUid());
        workflowStep.setInputData(simpleWorkflowStep.getInputData());
        return workflowStep;
    }

    public static JsonSchema mapToJsonSchema(JsonSchemaDTO schemaDTO) {
        if(schemaDTO == null){
            return null;
        }
        JsonSchema jsonSchema = new JsonSchema();
        jsonSchema.setSchema(schemaDTO.getSchema());
        jsonSchema.setSchemaVersion(schemaDTO.getSchemaVersion());
        jsonSchema.setName(schemaDTO.getName());
        jsonSchema.setDescription(schemaDTO.getDescription());
        return jsonSchema;
    }
}
