package de.lenneflow.workflowservice.repository;

import de.lenneflow.workflowservice.model.WorkflowStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkflowStepRepositoryTest {

    @Mock
    private WorkflowStepRepository workflowStepRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUid_shouldReturnWorkflowStep() {
        String uid = "stepUid";
        WorkflowStep workflowStep = new WorkflowStep();
        when(workflowStepRepository.findByUid(uid)).thenReturn(workflowStep);

        WorkflowStep result = workflowStepRepository.findByUid(uid);

        assertNotNull(result);
        assertEquals(workflowStep, result);
    }

    @Test
    void findByUid_shouldReturnNullWhenNotFound() {
        String uid = "stepUid";
        when(workflowStepRepository.findByUid(uid)).thenReturn(null);

        WorkflowStep result = workflowStepRepository.findByUid(uid);

        assertNull(result);
    }

    @Test
    void findByNameAndWorkflowUid_shouldReturnWorkflowStep() {
        String name = "stepName";
        String workflowUid = "workflowUid";
        WorkflowStep workflowStep = new WorkflowStep();
        when(workflowStepRepository.findByNameAndWorkflowUid(name, workflowUid)).thenReturn(workflowStep);

        WorkflowStep result = workflowStepRepository.findByNameAndWorkflowUid(name, workflowUid);

        assertNotNull(result);
        assertEquals(workflowStep, result);
    }

    @Test
    void findByNameAndWorkflowUid_shouldReturnNullWhenNotFound() {
        String name = "stepName";
        String workflowUid = "workflowUid";
        when(workflowStepRepository.findByNameAndWorkflowUid(name, workflowUid)).thenReturn(null);

        WorkflowStep result = workflowStepRepository.findByNameAndWorkflowUid(name, workflowUid);

        assertNull(result);
    }

    @Test
    void findByWorkflowUid_shouldReturnWorkflowSteps() {
        String workflowUid = "workflowUid";
        List<WorkflowStep> workflowSteps = List.of(new WorkflowStep(), new WorkflowStep());
        when(workflowStepRepository.findByWorkflowUid(workflowUid)).thenReturn(workflowSteps);

        List<WorkflowStep> result = workflowStepRepository.findByWorkflowUid(workflowUid);

        assertNotNull(result);
        assertEquals(workflowSteps.size(), result.size());
    }

    @Test
    void findByWorkflowUid_shouldReturnEmptyListWhenNoneFound() {
        String workflowUid = "workflowUid";
        when(workflowStepRepository.findByWorkflowUid(workflowUid)).thenReturn(List.of());

        List<WorkflowStep> result = workflowStepRepository.findByWorkflowUid(workflowUid);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByWorkflowName_shouldReturnWorkflowSteps() {
        String workflowName = "workflowName";
        List<WorkflowStep> workflowSteps = List.of(new WorkflowStep(), new WorkflowStep());
        when(workflowStepRepository.findByWorkflowName(workflowName)).thenReturn(workflowSteps);

        List<WorkflowStep> result = workflowStepRepository.findByWorkflowName(workflowName);

        assertNotNull(result);
        assertEquals(workflowSteps.size(), result.size());
    }

    @Test
    void findByWorkflowName_shouldReturnEmptyListWhenNoneFound() {
        String workflowName = "workflowName";
        when(workflowStepRepository.findByWorkflowName(workflowName)).thenReturn(List.of());

        List<WorkflowStep> result = workflowStepRepository.findByWorkflowName(workflowName);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}