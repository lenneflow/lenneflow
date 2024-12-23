package de.lenneflow.workflowservice.util;

import de.lenneflow.workflowservice.dto.WorkflowDTO;
import de.lenneflow.workflowservice.enums.ControlStructure;
import de.lenneflow.workflowservice.enums.JsonSchemaVersion;
import de.lenneflow.workflowservice.exception.InternalServiceException;
import de.lenneflow.workflowservice.exception.PayloadNotValidException;
import de.lenneflow.workflowservice.model.JsonSchema;
import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.model.WorkflowStep;
import de.lenneflow.workflowservice.repository.JsonSchemaRepository;
import de.lenneflow.workflowservice.repository.WorkflowRepository;
import de.lenneflow.workflowservice.repository.WorkflowStepRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidatorTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowStepRepository workflowStepRepository;

    @Mock
    private JsonSchemaRepository jsonSchemaRepository;

    private Validator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new Validator(workflowRepository, workflowStepRepository, jsonSchemaRepository);
    }

    @Test
    void validateWorkflowStep_shouldThrowExceptionWhenUidIsNull() {
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setUid(null);

        assertThrows(InternalServiceException.class, () -> validator.validate(workflowStep));
    }

    @Test
    void validateWorkflowStep_shouldThrowExceptionWhenWorkflowUidIsNull() {
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setUid("uid");
        workflowStep.setWorkflowUid(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(workflowStep));
    }

    @Test
    void validateWorkflowStep_shouldThrowExceptionWhenNameIsNull() {
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setUid("uid");
        workflowStep.setWorkflowUid("workflowUid");
        workflowStep.setName(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(workflowStep));
    }

    @Test
    void validateWorkflowStep_shouldThrowExceptionWhenExecutionOrderIsZero() {
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setUid("uid");
        workflowStep.setWorkflowUid("workflowUid");
        workflowStep.setName("name");
        workflowStep.setExecutionOrder(0);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(workflowStep));
    }

    @Test
    void validateWorkflowStep_shouldThrowExceptionWhenWorkflowDoesNotExist() {
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setUid("uid");
        workflowStep.setWorkflowUid("workflowUid");
        workflowStep.setName("name");
        workflowStep.setExecutionOrder(1);
        workflowStep.setControlStructure(ControlStructure.SIMPLE);

        when(workflowRepository.findByUid("workflowUid")).thenReturn(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(workflowStep));
    }

    @Test
    void validateWorkflowStep_shouldThrowExceptionWhenStepNameAlreadyExists() {
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setUid("uid");
        workflowStep.setWorkflowUid("workflowUid");
        workflowStep.setName("name");
        workflowStep.setExecutionOrder(1);
        workflowStep.setControlStructure(ControlStructure.SIMPLE);

        when(workflowRepository.findByUid("workflowUid")).thenReturn(new Workflow());
        when(workflowStepRepository.findByNameAndWorkflowUid("name", "workflowUid")).thenReturn(new WorkflowStep());

        assertThrows(PayloadNotValidException.class, () -> validator.validate(workflowStep));
    }

    @Test
    void validateWorkflowStep_shouldThrowExceptionWhenSimpleStepHasNoFunctionUid() {
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setUid("uid");
        workflowStep.setWorkflowUid("workflowUid");
        workflowStep.setName("name");
        workflowStep.setExecutionOrder(1);
        workflowStep.setControlStructure(ControlStructure.SIMPLE);

        when(workflowRepository.findByUid("workflowUid")).thenReturn(new Workflow());

        assertThrows(PayloadNotValidException.class, () -> validator.validate(workflowStep));
    }

    @Test
    void validateWorkflowStep_shouldThrowExceptionWhenSwitchStepHasNoSwitchCase() {
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setUid("uid");
        workflowStep.setWorkflowUid("workflowUid");
        workflowStep.setName("name");
        workflowStep.setExecutionOrder(1);
        workflowStep.setControlStructure(ControlStructure.SWITCH);

        when(workflowRepository.findByUid("workflowUid")).thenReturn(new Workflow());

        assertThrows(PayloadNotValidException.class, () -> validator.validate(workflowStep));
    }

    @Test
    void validateWorkflowStep_shouldThrowExceptionWhenSwitchStepHasNoDecisionCases() {
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setUid("uid");
        workflowStep.setWorkflowUid("workflowUid");
        workflowStep.setName("name");
        workflowStep.setExecutionOrder(1);
        workflowStep.setControlStructure(ControlStructure.SWITCH);
        workflowStep.setSwitchCase("switchCase");

        when(workflowRepository.findByUid("workflowUid")).thenReturn(new Workflow());

        assertThrows(PayloadNotValidException.class, () -> validator.validate(workflowStep));
    }

    @Test
    void validateWorkflowStep_shouldThrowExceptionWhenDoWhileStepHasNoFunctionUid() {
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setUid("uid");
        workflowStep.setWorkflowUid("workflowUid");
        workflowStep.setName("name");
        workflowStep.setExecutionOrder(1);
        workflowStep.setControlStructure(ControlStructure.DO_WHILE);

        when(workflowRepository.findByUid("workflowUid")).thenReturn(new Workflow());

        assertThrows(PayloadNotValidException.class, () -> validator.validate(workflowStep));
    }

    @Test
    void validateWorkflowStep_shouldThrowExceptionWhenDoWhileStepHasNoStopCondition() {
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setUid("uid");
        workflowStep.setWorkflowUid("workflowUid");
        workflowStep.setName("name");
        workflowStep.setExecutionOrder(1);
        workflowStep.setControlStructure(ControlStructure.DO_WHILE);
        workflowStep.setFunctionUid("functionUid");

        when(workflowRepository.findByUid("workflowUid")).thenReturn(new Workflow());

        assertThrows(PayloadNotValidException.class, () -> validator.validate(workflowStep));
    }

    @Test
    void validateWorkflowStep_shouldThrowExceptionWhenSubWorkflowStepHasNoSubWorkflowUid() {
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setUid("uid");
        workflowStep.setWorkflowUid("workflowUid");
        workflowStep.setName("name");
        workflowStep.setExecutionOrder(1);
        workflowStep.setControlStructure(ControlStructure.SUB_WORKFLOW);

        when(workflowRepository.findByUid("workflowUid")).thenReturn(new Workflow());

        assertThrows(PayloadNotValidException.class, () -> validator.validate(workflowStep));
    }

    @Test
    void validateWorkflow_shouldThrowExceptionWhenNameIsEmpty() {
        Workflow workflow = new Workflow();
        workflow.setName("");

        assertThrows(PayloadNotValidException.class, () -> validator.validate(workflow));
    }

    @Test
    void validateWorkflow_shouldThrowExceptionWhenNameAlreadyExists() {
        Workflow workflow = new Workflow();
        workflow.setName("name");

        when(workflowRepository.findByName("name")).thenReturn(new Workflow());

        assertThrows(PayloadNotValidException.class, () -> validator.validate(workflow));
    }

    @Test
    void validateJsonSchema_shouldThrowExceptionWhenSchemaIsInvalid() {
        JsonSchema jsonSchema = new JsonSchema();
        jsonSchema.setSchemaVersion(JsonSchemaVersion.V4);
        jsonSchema.setSchema("invalidSchema");

        assertThrows(PayloadNotValidException.class, () -> validator.validateJsonSchema(jsonSchema));
    }

    @Test
    void validateWorkflowDTO_shouldThrowExceptionWhenInputDataSchemaUidIsNull() {
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setInputDataSchemaUid(null);

        assertThrows(PayloadNotValidException.class, () -> validator.validate(workflowDTO));
    }
}