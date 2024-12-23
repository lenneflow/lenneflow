package de.lenneflow.workflowservice.util;

import de.lenneflow.workflowservice.dto.*;
import de.lenneflow.workflowservice.enums.ControlStructure;
import de.lenneflow.workflowservice.enums.JsonSchemaVersion;
import de.lenneflow.workflowservice.model.JsonSchema;
import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.model.WorkflowStep;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ObjectMapperTest {

    @Test
    void mapToWorkflow_shouldMapWorkflowDTOToWorkflow() {
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setDescription("Test Description");
        workflowDTO.setName("Test Name");

        Workflow result = ObjectMapper.mapToWorkflow(workflowDTO);

        assertNotNull(result);
        assertEquals("Test Description", result.getDescription());
        assertEquals("Test Name", result.getName());
        assertEquals(Long.MAX_VALUE, result.getTimeOutInSeconds());
    }

    @Test
    void mapToWorkflowStep_shouldMapSubWorkflowStepToWorkflowStep() {
        SubWorkflowStep subWorkflowStep = new SubWorkflowStep();
        subWorkflowStep.setName("SubWorkflowStep");
        subWorkflowStep.setWorkflowUid("workflowUid");
        subWorkflowStep.setSubWorkflowUid("subWorkflowUid");
        subWorkflowStep.setDescription("Description");
        subWorkflowStep.setExecutionOrder(1);
        subWorkflowStep.setInputData(new HashMap<>());
        subWorkflowStep.setRetryCount(3);

        WorkflowStep result = ObjectMapper.mapToWorkflowStep(subWorkflowStep);

        assertNotNull(result);
        assertEquals("SubWorkflowStep", result.getName());
        assertEquals("workflowUid", result.getWorkflowUid());
        assertEquals("subWorkflowUid", result.getSubWorkflowUid());
        assertEquals("Description", result.getDescription());
        assertEquals(ControlStructure.SUB_WORKFLOW, result.getControlStructure());
        assertEquals(1, result.getExecutionOrder());
        assertEquals(new HashMap<>(), result.getInputData());
        assertEquals(3, result.getRetryCount());
    }

    @Test
    void mapToJsonSchema_shouldMapJsonSchemaDTOToJsonSchema() {
        JsonSchemaDTO schemaDTO = new JsonSchemaDTO();
        schemaDTO.setSchema("schema");
        schemaDTO.setSchemaVersion(JsonSchemaVersion.V4);
        schemaDTO.setName("Schema Name");
        schemaDTO.setDescription("Schema Description");

        JsonSchema result = ObjectMapper.mapToJsonSchema(schemaDTO);

        assertNotNull(result);
        assertEquals("schema", result.getSchema());
        assertEquals(JsonSchemaVersion.V4, result.getSchemaVersion());
        assertEquals("Schema Name", result.getName());
        assertEquals("Schema Description", result.getDescription());
    }

    @Test
    void mapToWorkflowStep_shouldMapWhileWorkflowStepToWorkflowStep() {
        WhileWorkflowStep whileWorkflowStep = new WhileWorkflowStep();
        whileWorkflowStep.setName("WhileWorkflowStep");
        whileWorkflowStep.setWorkflowUid("workflowUid");
        whileWorkflowStep.setDescription("Description");
        whileWorkflowStep.setExecutionOrder(2);
        whileWorkflowStep.setFunctionUid("functionUid");
        whileWorkflowStep.setStopCondition("stopCondition");
        whileWorkflowStep.setInputData(new HashMap<>());
        whileWorkflowStep.setRetryCount(2);

        WorkflowStep result = ObjectMapper.mapToWorkflowStep(whileWorkflowStep);

        assertNotNull(result);
        assertEquals("WhileWorkflowStep", result.getName());
        assertEquals("workflowUid", result.getWorkflowUid());
        assertEquals("Description", result.getDescription());
        assertEquals(ControlStructure.DO_WHILE, result.getControlStructure());
        assertEquals(2, result.getExecutionOrder());
        assertEquals("functionUid", result.getFunctionUid());
        assertEquals("stopCondition", result.getStopCondition());
        assertEquals(new HashMap<>(), result.getInputData());
        assertEquals(2, result.getRetryCount());
    }

    @Test
    void mapToWorkflowStep_shouldMapSwitchWorkflowStepToWorkflowStep() {
        SwitchWorkflowStep switchWorkflowStep = new SwitchWorkflowStep();
        switchWorkflowStep.setName("SwitchWorkflowStep");
        switchWorkflowStep.setWorkflowUid("workflowUid");
        switchWorkflowStep.setDescription("Description");
        switchWorkflowStep.setExecutionOrder(3);
        switchWorkflowStep.setRetryCount(1);
        switchWorkflowStep.setSwitchCase("switchCase");
        switchWorkflowStep.setDecisionCases(new ArrayList<>());
        switchWorkflowStep.setInputData(new HashMap<>());

        WorkflowStep result = ObjectMapper.mapToWorkflowStep(switchWorkflowStep);

        assertNotNull(result);
        assertEquals("SwitchWorkflowStep", result.getName());
        assertEquals("workflowUid", result.getWorkflowUid());
        assertEquals("Description", result.getDescription());
        assertEquals(ControlStructure.SWITCH, result.getControlStructure());
        assertEquals(3, result.getExecutionOrder());
        assertEquals(1, result.getRetryCount());
        assertEquals("switchCase", result.getSwitchCase());
        assertEquals(new ArrayList<>(), result.getDecisionCases());
        assertEquals(new HashMap<>(), result.getInputData());
    }

    @Test
    void mapToWorkflowStep_shouldMapSimpleWorkflowStepToWorkflowStep() {
        SimpleWorkflowStep simpleWorkflowStep = new SimpleWorkflowStep();
        simpleWorkflowStep.setName("SimpleWorkflowStep");
        simpleWorkflowStep.setWorkflowUid("workflowUid");
        simpleWorkflowStep.setDescription("Description");
        simpleWorkflowStep.setExecutionOrder(4);
        simpleWorkflowStep.setRetryCount(0);
        simpleWorkflowStep.setFunctionUid("functionUid");
        simpleWorkflowStep.setInputData(new HashMap<>());

        WorkflowStep result = ObjectMapper.mapToWorkflowStep(simpleWorkflowStep);

        assertNotNull(result);
        assertEquals("SimpleWorkflowStep", result.getName());
        assertEquals("workflowUid", result.getWorkflowUid());
        assertEquals("Description", result.getDescription());
        assertEquals(ControlStructure.SIMPLE, result.getControlStructure());
        assertEquals(4, result.getExecutionOrder());
        assertEquals(0, result.getRetryCount());
        assertEquals("functionUid", result.getFunctionUid());
        assertEquals(new HashMap<>(), result.getInputData());
    }

    @Test
    void mapToWorkflow_shouldHandleNullWorkflowDTO() {
        WorkflowDTO workflowDTO = null;

        Workflow result = ObjectMapper.mapToWorkflow(workflowDTO);

        assertNull(result);
    }

    @Test
    void mapToWorkflowStep_shouldHandleNullSubWorkflowStep() {
        SubWorkflowStep subWorkflowStep = null;

        WorkflowStep result = ObjectMapper.mapToWorkflowStep(subWorkflowStep);

        assertNull(result);
    }

    @Test
    void mapToWorkflowStep_shouldHandleNullWhileWorkflowStep() {
        WhileWorkflowStep whileWorkflowStep = null;

        WorkflowStep result = ObjectMapper.mapToWorkflowStep(whileWorkflowStep);

        assertNull(result);
    }

    @Test
    void mapToWorkflowStep_shouldHandleNullSwitchWorkflowStep() {
        SwitchWorkflowStep switchWorkflowStep = null;

        WorkflowStep result = ObjectMapper.mapToWorkflowStep(switchWorkflowStep);

        assertNull(result);
    }

    @Test
    void mapToWorkflowStep_shouldHandleNullSimpleWorkflowStep() {
        SimpleWorkflowStep simpleWorkflowStep = null;

        WorkflowStep result = ObjectMapper.mapToWorkflowStep(simpleWorkflowStep);

        assertNull(result);
    }

    @Test
    void mapToJsonSchema_shouldHandleNullJsonSchemaDTO() {
        JsonSchemaDTO schemaDTO = null;

        JsonSchema result = ObjectMapper.mapToJsonSchema(schemaDTO);

        assertNull(result);
    }

    @Test
    void mapToWorkflowStep_shouldMapSubWorkflowStepWithNullFields() {
        SubWorkflowStep subWorkflowStep = new SubWorkflowStep();

        WorkflowStep result = ObjectMapper.mapToWorkflowStep(subWorkflowStep);

        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getWorkflowUid());
        assertNull(result.getSubWorkflowUid());
        assertNull(result.getDescription());
        assertEquals(ControlStructure.SUB_WORKFLOW, result.getControlStructure());
        assertEquals(0, result.getExecutionOrder());
        assertEquals(0, result.getRetryCount());
    }

    @Test
    void mapToWorkflowStep_shouldMapWhileWorkflowStepWithNullFields() {
        WhileWorkflowStep whileWorkflowStep = new WhileWorkflowStep();

        WorkflowStep result = ObjectMapper.mapToWorkflowStep(whileWorkflowStep);

        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getWorkflowUid());
        assertNull(result.getDescription());
        assertEquals(ControlStructure.DO_WHILE, result.getControlStructure());
        assertEquals(0, result.getExecutionOrder());
        assertNull(result.getFunctionUid());
        assertNull(result.getStopCondition());
        assertEquals(0, result.getRetryCount());
    }

    @Test
    void mapToWorkflowStep_shouldMapSwitchWorkflowStepWithNullFields() {
        SwitchWorkflowStep switchWorkflowStep = new SwitchWorkflowStep();

        WorkflowStep result = ObjectMapper.mapToWorkflowStep(switchWorkflowStep);

        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getWorkflowUid());
        assertNull(result.getDescription());
        assertEquals(ControlStructure.SWITCH, result.getControlStructure());
        assertEquals(0, result.getExecutionOrder());
        assertNull(result.getSwitchCase());
        assertEquals(0, result.getRetryCount());
    }

    @Test
    void mapToWorkflowStep_shouldMapSimpleWorkflowStepWithNullFields() {
        SimpleWorkflowStep simpleWorkflowStep = new SimpleWorkflowStep();

        WorkflowStep result = ObjectMapper.mapToWorkflowStep(simpleWorkflowStep);

        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getWorkflowUid());
        assertNull(result.getDescription());
        assertEquals(ControlStructure.SIMPLE, result.getControlStructure());
        assertEquals(0, result.getExecutionOrder());
        assertNull(result.getFunctionUid());
        assertEquals(0, result.getRetryCount());
    }
}