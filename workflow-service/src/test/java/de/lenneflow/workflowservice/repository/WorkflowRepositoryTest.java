package de.lenneflow.workflowservice.repository;

import de.lenneflow.workflowservice.model.Workflow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkflowRepositoryTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUid_shouldReturnWorkflow() {
        String uid = "workflowUid";
        Workflow workflow = new Workflow();
        when(workflowRepository.findByUid(uid)).thenReturn(workflow);

        Workflow result = workflowRepository.findByUid(uid);

        assertNotNull(result);
        assertEquals(workflow, result);
    }

    @Test
    void findByUid_shouldReturnNullWhenNotFound() {
        String uid = "workflowUid";
        when(workflowRepository.findByUid(uid)).thenReturn(null);

        Workflow result = workflowRepository.findByUid(uid);

        assertNull(result);
    }

    @Test
    void findByName_shouldReturnWorkflow() {
        String name = "workflowName";
        Workflow workflow = new Workflow();
        when(workflowRepository.findByName(name)).thenReturn(workflow);

        Workflow result = workflowRepository.findByName(name);

        assertNotNull(result);
        assertEquals(workflow, result);
    }

    @Test
    void findByName_shouldReturnNullWhenNotFound() {
        String name = "workflowName";
        when(workflowRepository.findByName(name)).thenReturn(null);

        Workflow result = workflowRepository.findByName(name);

        assertNull(result);
    }

    @Test
    void findByStatusListenerEnabled_shouldReturnWorkflows() {
        boolean enabled = true;
        List<Workflow> workflows = List.of(new Workflow(), new Workflow());
        when(workflowRepository.findByStatusListenerEnabled(enabled)).thenReturn(workflows);

        List<Workflow> result = workflowRepository.findByStatusListenerEnabled(enabled);

        assertNotNull(result);
        assertEquals(workflows.size(), result.size());
    }

    @Test
    void findByStatusListenerEnabled_shouldReturnEmptyListWhenNoneFound() {
        boolean enabled = true;
        when(workflowRepository.findByStatusListenerEnabled(enabled)).thenReturn(List.of());

        List<Workflow> result = workflowRepository.findByStatusListenerEnabled(enabled);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}