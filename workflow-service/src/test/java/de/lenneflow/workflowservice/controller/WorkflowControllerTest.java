package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.dto.JsonSchemaDTO;
import de.lenneflow.workflowservice.dto.WorkflowDTO;
import de.lenneflow.workflowservice.model.JsonSchema;
import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.repository.JsonSchemaRepository;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import de.lenneflow.workflowservice.repository.WorkflowStepRepository;
import de.lenneflow.workflowservice.util.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkflowControllerTest {

    @Mock
    private WorkflowRepository workflowRepository;
    @Mock
    private WorkflowStepRepository workflowStepRepository;
    @Mock
    private JsonSchemaRepository jsonSchemaRepository;
    @Mock
    private Validator validator;

    private WorkflowController workflowController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workflowController = new WorkflowController(workflowRepository, workflowStepRepository, jsonSchemaRepository, validator);
    }

    @Test
    void getWorkflow_shouldReturnWorkflow() {
        String uid = "workflowUid";
        Workflow workflow = new Workflow();
        when(workflowRepository.findByUid(uid)).thenReturn(workflow);

        Workflow result = workflowController.getWorkflow(uid);

        assertNotNull(result);
        assertEquals(workflow, result);
    }

    @Test
    void getWorkflow_shouldReturnNullWhenNotFound() {
        String uid = "workflowUid";
        when(workflowRepository.findByUid(uid)).thenReturn(null);

        Workflow result = workflowController.getWorkflow(uid);

        assertNull(result);
    }

    @Test
    void getWorkflowByName_shouldReturnWorkflow() {
        String name = "workflowName";
        Workflow workflow = new Workflow();
        when(workflowRepository.findByName(name)).thenReturn(workflow);

        Workflow result = workflowController.getWorkflowByName(name);

        assertNotNull(result);
        assertEquals(workflow, result);
    }

    @Test
    void getWorkflowByName_shouldReturnNullWhenNotFound() {
        String name = "workflowName";
        when(workflowRepository.findByName(name)).thenReturn(null);

        Workflow result = workflowController.getWorkflowByName(name);

        assertNull(result);
    }

    @Test
    void getAllWorkflows_shouldReturnWorkflowList() {
        List<Workflow> workflows = List.of(new Workflow(), new Workflow());
        when(workflowRepository.findAll()).thenReturn(workflows);

        List<Workflow> result = workflowController.getAllWorkflows();

        assertNotNull(result);
        assertEquals(workflows.size(), result.size());
    }

    @Test
    void addNewWorkflow_shouldCreateAndReturnWorkflow() {
        WorkflowDTO workflowDTO = new WorkflowDTO();
        Workflow workflow = new Workflow();
        JsonSchema inputSchema = new JsonSchema();
        JsonSchema outputSchema = new JsonSchema();

        when(jsonSchemaRepository.findByUid(workflowDTO.getInputDataSchemaUid())).thenReturn(inputSchema);
        when(jsonSchemaRepository.findByUid(workflowDTO.getOutputDataSchemaUid())).thenReturn(outputSchema);
        when(workflowRepository.save(any(Workflow.class))).thenReturn(workflow);

        Workflow result = workflowController.addNewWorkflow(workflowDTO);

        assertNotNull(result);
        assertEquals(workflow, result);
    }

    @Test
    void addNewWorkflow_shouldThrowExceptionWhenValidationFails() {
        WorkflowDTO workflowDTO = new WorkflowDTO();
        doThrow(new RuntimeException("Validation failed")).when(validator).validate(workflowDTO);

        assertThrows(RuntimeException.class, () -> workflowController.addNewWorkflow(workflowDTO));
    }

    @Test
    void addJsonSchema_shouldCreateAndReturnJsonSchema() {
        JsonSchemaDTO jsonSchemaDTO = new JsonSchemaDTO();
        JsonSchema jsonSchema = new JsonSchema();

        when(jsonSchemaRepository.save(any(JsonSchema.class))).thenReturn(jsonSchema);

        JsonSchema result = workflowController.addJsonSchema(jsonSchemaDTO);

        assertNotNull(result);
        assertEquals(jsonSchema, result);
    }

    @Test
    void getJsonSchemaList_shouldReturnJsonSchemaList() {
        List<JsonSchema> jsonSchemas = List.of(new JsonSchema(), new JsonSchema());
        when(jsonSchemaRepository.findAll()).thenReturn(jsonSchemas);

        List<JsonSchema> result = workflowController.getJsonSchemaList();

        assertNotNull(result);
        assertEquals(jsonSchemas.size(), result.size());
    }

    @Test
    void deleteWorkflow_shouldDeleteWorkflowAndSteps() {
        String uid = "workflowUid";
        Workflow workflow = new Workflow();
        when(workflowRepository.findByUid(uid)).thenReturn(workflow);

        workflowController.deleteWorkflow(uid);

        verify(workflowStepRepository).deleteAll(workflow.getSteps());
        verify(workflowRepository).delete(workflow);
    }
}