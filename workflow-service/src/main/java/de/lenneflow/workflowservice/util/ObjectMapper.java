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
        Workflow workflow = new Workflow();
        workflow.setDescription(workflowDTO.getDescription());
        workflow.setName(workflowDTO.getName());
        workflow.setTimeOutInSeconds(workflowDTO.getTimeOutInSeconds());
        return workflow;
    }

    public static WorkflowStep mapToWorkflowStep(WorkflowStep workflowStep, SubWorkflowStep subWorkflowStep) {
        workflowStep.setName(subWorkflowStep.getName());
        workflowStep.setWorkflowUid(subWorkflowStep.getWorkflowUid());
        workflowStep.setSubWorkflowId(subWorkflowStep.getSubWorkflowUid());
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
        workflowStep.setName(whileWorkflowStep.getName());
        workflowStep.setWorkflowUid(whileWorkflowStep.getWorkflowUid());
        workflowStep.setDescription(whileWorkflowStep.getDescription());
        workflowStep.setControlStructure(ControlStructure.DO_WHILE);
        workflowStep.setExecutionOrder(whileWorkflowStep.getExecutionOrder());
        workflowStep.setFunctionId(whileWorkflowStep.getFunctionUid());
        workflowStep.setStopCondition(whileWorkflowStep.getStopCondition());
        workflowStep.setInputData(whileWorkflowStep.getInputData());
        workflowStep.setRetryCount(whileWorkflowStep.getRetryCount());
        return workflowStep;

    }

    public static WorkflowStep mapToWorkflowStep(SwitchWorkflowStep switchWorkflowStep) {
        return mapToWorkflowStep(new WorkflowStep(), switchWorkflowStep);
    }

    public static WorkflowStep mapToWorkflowStep(WorkflowStep workflowStep, SwitchWorkflowStep switchWorkflowStep) {
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
        workflowStep.setName(simpleWorkflowStep.getName());
        workflowStep.setWorkflowUid(simpleWorkflowStep.getWorkflowUid());
        workflowStep.setDescription(simpleWorkflowStep.getDescription());
        workflowStep.setExecutionOrder(simpleWorkflowStep.getExecutionOrder());
        workflowStep.setControlStructure(ControlStructure.SIMPLE);
        workflowStep.setRetryCount(simpleWorkflowStep.getRetryCount());
        workflowStep.setFunctionId(simpleWorkflowStep.getFunctionUid());
        workflowStep.setInputData(simpleWorkflowStep.getInputData());
        return workflowStep;
    }

    public static JsonSchema mapToJsonSchema(JsonSchemaDTO schemaDTO) {
        JsonSchema jsonSchema = new JsonSchema();
        jsonSchema.setSchema(schemaDTO.getSchema());
        jsonSchema.setSchemaVersion(schemaDTO.getSchemaVersion());
        jsonSchema.setName(schemaDTO.getName());
        jsonSchema.setDescription(schemaDTO.getDescription());
        return jsonSchema;
    }
}
