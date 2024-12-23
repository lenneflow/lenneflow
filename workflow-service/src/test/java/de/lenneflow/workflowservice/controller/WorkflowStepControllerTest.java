package de.lenneflow.workflowservice.controller;

import de.lenneflow.workflowservice.dto.SimpleWorkflowStep;
import de.lenneflow.workflowservice.dto.SubWorkflowStep;
import de.lenneflow.workflowservice.dto.SwitchWorkflowStep;
import de.lenneflow.workflowservice.dto.WhileWorkflowStep;
import de.lenneflow.workflowservice.model.Workflow;
import de.lenneflow.workflowservice.model.WorkflowStep;
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

class WorkflowStepControllerTest {

    @Mock
    private WorkflowStepRepository workflowStepRepository;
    @Mock
    private WorkflowRepository workflowRepository;
    @Mock
    private Validator validator;

    private WorkflowStepController workflowStepController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workflowStepController = new WorkflowStepController(workflowStepRepository, workflowRepository, validator);
    }

    @Test
    void getStep_shouldReturnWorkflowStep() {
        String stepId = "stepUid";
        WorkflowStep workflowStep = new WorkflowStep();
        when(workflowStepRepository.findByUid(stepId)).thenReturn(workflowStep);

        WorkflowStep result = workflowStepController.getStep(stepId);

        assertNotNull(result);
        assertEquals(workflowStep, result);
    }

    @Test
    void getStep_shouldReturnNullWhenNotFound() {
        String stepId = "stepUid";
        when(workflowStepRepository.findByUid(stepId)).thenReturn(null);

        WorkflowStep result = workflowStepController.getStep(stepId);

        assertNull(result);
    }

    @Test
    void getStepByNameAndWorkflowId_shouldReturnWorkflowStep() {
        String name = "stepName";
        String workflowId = "workflowUid";
        WorkflowStep workflowStep = new WorkflowStep();
        when(workflowStepRepository.findByNameAndWorkflowUid(name, workflowId)).thenReturn(workflowStep);

        WorkflowStep result = workflowStepController.getStepByNameAndWorkflowId(name, workflowId);

        assertNotNull(result);
        assertEquals(workflowStep, result);
    }

    @Test
    void getStepByNameAndWorkflowId_shouldReturnNullWhenNotFound() {
        String name = "stepName";
        String workflowId = "workflowUid";
        when(workflowStepRepository.findByNameAndWorkflowUid(name, workflowId)).thenReturn(null);

        WorkflowStep result = workflowStepController.getStepByNameAndWorkflowId(name, workflowId);

        assertNull(result);
    }

    @Test
    void getWorkflowStepsByWorkflowID_shouldReturnWorkflowSteps() {
        String workflowId = "workflowUid";
        List<WorkflowStep> workflowSteps = List.of(new WorkflowStep(), new WorkflowStep());
        when(workflowStepRepository.findByWorkflowUid(workflowId)).thenReturn(workflowSteps);

        List<WorkflowStep> result = workflowStepController.getWorkflowStepsByWorkflowID(workflowId);

        assertNotNull(result);
        assertEquals(workflowSteps.size(), result.size());
    }

    @Test
    void getAllWorkflowStepsByWorkflowName_shouldReturnWorkflowSteps() {
        String workflowName = "workflowName";
        List<WorkflowStep> workflowSteps = List.of(new WorkflowStep(), new WorkflowStep());
        when(workflowStepRepository.findByWorkflowName(workflowName)).thenReturn(workflowSteps);

        List<WorkflowStep> result = workflowStepController.getAllWorkflowStepsByWorkflowName(workflowName);

        assertNotNull(result);
        assertEquals(workflowSteps.size(), result.size());
    }

    @Test
    void getAllWorkflowSteps_shouldReturnAllWorkflowSteps() {
        List<WorkflowStep> workflowSteps = List.of(new WorkflowStep(), new WorkflowStep());
        when(workflowStepRepository.findAll()).thenReturn(workflowSteps);

        List<WorkflowStep> result = workflowStepController.getAllWorkflowSteps();

        assertNotNull(result);
        assertEquals(workflowSteps.size(), result.size());
    }

    @Test
    void addSimpleWorkflowStep_shouldCreateAndReturnWorkflowStep() {
        SimpleWorkflowStep simpleWorkflowStep = new SimpleWorkflowStep();
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setWorkflowUid("uid");

        when(workflowStepRepository.save(any(WorkflowStep.class))).thenReturn(workflowStep);
        when(workflowRepository.findByUid(anyString())).thenReturn(new Workflow());

        WorkflowStep result = workflowStepController.addSimpleWorkflowStep(simpleWorkflowStep);

        assertNotNull(result);
        assertEquals(workflowStep, result);
    }

    @Test
    void addSwitchWorkflowStep_shouldCreateAndReturnWorkflowStep() {
        SwitchWorkflowStep switchWorkflowStep = new SwitchWorkflowStep();
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setWorkflowUid("uid");

        when(workflowStepRepository.save(any(WorkflowStep.class))).thenReturn(workflowStep);
        when(workflowRepository.findByUid(anyString())).thenReturn(new Workflow());

        WorkflowStep result = workflowStepController.addSwitchWorkflowStep(switchWorkflowStep);

        assertNotNull(result);
        assertEquals(workflowStep, result);
    }

    @Test
    void addWhileWorkflowStep_shouldCreateAndReturnWorkflowStep() {
        WhileWorkflowStep whileWorkflowStep = new WhileWorkflowStep();
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setWorkflowUid("uid");

        when(workflowStepRepository.save(any(WorkflowStep.class))).thenReturn(workflowStep);
        when(workflowRepository.findByUid(anyString())).thenReturn(new Workflow());

        WorkflowStep result = workflowStepController.addWhileWorkflowStep(whileWorkflowStep);

        assertNotNull(result);
        assertEquals(workflowStep, result);
    }

    @Test
    void addSubWorkflowStep_shouldCreateAndReturnWorkflowStep() {
        SubWorkflowStep subWorkflowStep = new SubWorkflowStep();
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setWorkflowUid("uid");

        when(workflowStepRepository.save(any(WorkflowStep.class))).thenReturn(workflowStep);
        when(workflowRepository.findByUid(anyString())).thenReturn(new Workflow());

        WorkflowStep result = workflowStepController.addSubWorkflowStep(subWorkflowStep);

        assertNotNull(result);
        assertEquals(workflowStep, result);
    }

    @Test
    void deleteWorkflowStep_shouldDeleteWorkflowStep() {
        String uid = "stepUid";
        WorkflowStep workflowStep = new WorkflowStep();
        when(workflowStepRepository.findByUid(uid)).thenReturn(workflowStep);

        workflowStepController.deleteWorkflowStep(uid);

        verify(workflowStepRepository).delete(workflowStep);
    }

    @Test
    void updateSimpleWorkflowStep_shouldUpdateAndReturnWorkflowStep() {
        String uid = "stepUid";
        SimpleWorkflowStep simpleWorkflowStep = new SimpleWorkflowStep();
        WorkflowStep workflowStep = new WorkflowStep();
        when(workflowStepRepository.findByUid(uid)).thenReturn(workflowStep);
        when(workflowStepRepository.save(any(WorkflowStep.class))).thenReturn(workflowStep);

        WorkflowStep result = workflowStepController.updateWorkflowStep(uid, simpleWorkflowStep);

        assertNotNull(result);
        assertEquals(workflowStep, result);
    }

    @Test
    void updateSwitchWorkflowStep_shouldUpdateAndReturnWorkflowStep() {
        String uid = "stepUid";
        SwitchWorkflowStep switchWorkflowStep = new SwitchWorkflowStep();
        WorkflowStep workflowStep = new WorkflowStep();
        when(workflowStepRepository.findByUid(uid)).thenReturn(workflowStep);
        when(workflowStepRepository.save(any(WorkflowStep.class))).thenReturn(workflowStep);

        WorkflowStep result = workflowStepController.updateWorkflowStep(uid, switchWorkflowStep);

        assertNotNull(result);
        assertEquals(workflowStep, result);
    }

    @Test
    void updateWhileWorkflowStep_shouldUpdateAndReturnWorkflowStep() {
        String uid = "stepUid";
        WhileWorkflowStep whileWorkflowStep = new WhileWorkflowStep();
        WorkflowStep workflowStep = new WorkflowStep();
        when(workflowStepRepository.findByUid(uid)).thenReturn(workflowStep);
        when(workflowStepRepository.save(any(WorkflowStep.class))).thenReturn(workflowStep);

        WorkflowStep result = workflowStepController.updateWorkflowStep(uid, whileWorkflowStep);

        assertNotNull(result);
        assertEquals(workflowStep, result);
    }

    @Test
    void updateSubWorkflowStep_shouldUpdateAndReturnWorkflowStep() {
        String uid = "stepUid";
        SubWorkflowStep subWorkflowStep = new SubWorkflowStep();
        WorkflowStep workflowStep = new WorkflowStep();
        when(workflowStepRepository.findByUid(uid)).thenReturn(workflowStep);
        when(workflowStepRepository.save(any(WorkflowStep.class))).thenReturn(workflowStep);

        WorkflowStep result = workflowStepController.updateWorkflowStep(uid, subWorkflowStep);

        assertNotNull(result);
        assertEquals(workflowStep, result);
    }

    @Test
    void addSimpleWorkflowStep_shouldThrowExceptionWhenValidationFails() {
        SimpleWorkflowStep simpleWorkflowStep = new SimpleWorkflowStep();
        doThrow(new RuntimeException("Validation failed")).when(validator).validate(any(WorkflowStep.class));

        assertThrows(RuntimeException.class, () -> workflowStepController.addSimpleWorkflowStep(simpleWorkflowStep));
    }

    @Test
    void addSwitchWorkflowStep_shouldThrowExceptionWhenValidationFails() {
        SwitchWorkflowStep switchWorkflowStep = new SwitchWorkflowStep();
        doThrow(new RuntimeException("Validation failed")).when(validator).validate(any(WorkflowStep.class));

        assertThrows(RuntimeException.class, () -> workflowStepController.addSwitchWorkflowStep(switchWorkflowStep));
    }

    @Test
    void addWhileWorkflowStep_shouldThrowExceptionWhenValidationFails() {
        WhileWorkflowStep whileWorkflowStep = new WhileWorkflowStep();
        doThrow(new RuntimeException("Validation failed")).when(validator).validate(any(WorkflowStep.class));

        assertThrows(RuntimeException.class, () -> workflowStepController.addWhileWorkflowStep(whileWorkflowStep));
    }

    @Test
    void addSubWorkflowStep_shouldThrowExceptionWhenValidationFails() {
        SubWorkflowStep subWorkflowStep = new SubWorkflowStep();
        doThrow(new RuntimeException("Validation failed")).when(validator).validate(any(WorkflowStep.class));

        assertThrows(RuntimeException.class, () -> workflowStepController.addSubWorkflowStep(subWorkflowStep));
    }
}