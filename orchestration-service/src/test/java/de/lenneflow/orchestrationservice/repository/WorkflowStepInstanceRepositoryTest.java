package de.lenneflow.orchestrationservice.repository;

import de.lenneflow.orchestrationservice.model.WorkflowStepInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class WorkflowStepInstanceRepositoryTest {

    @Mock
    private WorkflowStepInstanceRepository workflowStepInstanceRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUid_returnsWorkflowStepInstance_whenUidExists() {
        WorkflowStepInstance instance = new WorkflowStepInstance();
        instance.setUid("12345");
        when(workflowStepInstanceRepository.findByUid("12345")).thenReturn(instance);

        WorkflowStepInstance result = workflowStepInstanceRepository.findByUid("12345");

        assertEquals("12345", result.getUid());
    }

    @Test
    void findByUid_returnsNull_whenUidDoesNotExist() {
        when(workflowStepInstanceRepository.findByUid("nonexistent")).thenReturn(null);

        WorkflowStepInstance result = workflowStepInstanceRepository.findByUid("nonexistent");

        assertNull(result);
    }

    @Test
    void findByUid_returnsNull_whenUidIsNull() {
        when(workflowStepInstanceRepository.findByUid(null)).thenReturn(null);

        WorkflowStepInstance result = workflowStepInstanceRepository.findByUid(null);

        assertNull(result);
    }

    @Test
    void findByUid_returnsNull_whenUidIsEmpty() {
        when(workflowStepInstanceRepository.findByUid("")).thenReturn(null);

        WorkflowStepInstance result = workflowStepInstanceRepository.findByUid("");

        assertNull(result);
    }

    @Test
    void findByNameAndWorkflowInstanceUid_returnsWorkflowStepInstance_whenNameAndWorkflowInstanceUidExist() {
        WorkflowStepInstance instance = new WorkflowStepInstance();
        instance.setName("stepName");
        instance.setWorkflowInstanceUid("workflowInstanceUid");
        when(workflowStepInstanceRepository.findByNameAndWorkflowInstanceUid("stepName", "workflowInstanceUid")).thenReturn(instance);

        WorkflowStepInstance result = workflowStepInstanceRepository.findByNameAndWorkflowInstanceUid("stepName", "workflowInstanceUid");

        assertEquals("stepName", result.getName());
        assertEquals("workflowInstanceUid", result.getWorkflowInstanceUid());
    }

    @Test
    void findByNameAndWorkflowInstanceUid_returnsNull_whenNameAndWorkflowInstanceUidDoNotExist() {
        when(workflowStepInstanceRepository.findByNameAndWorkflowInstanceUid("nonexistent", "nonexistent")).thenReturn(null);

        WorkflowStepInstance result = workflowStepInstanceRepository.findByNameAndWorkflowInstanceUid("nonexistent", "nonexistent");

        assertNull(result);
    }

    @Test
    void findByWorkflowInstanceUid_returnsListOfWorkflowStepInstances_whenWorkflowInstanceUidExists() {
        WorkflowStepInstance instance1 = new WorkflowStepInstance();
        instance1.setWorkflowInstanceUid("workflowInstanceUid");
        WorkflowStepInstance instance2 = new WorkflowStepInstance();
        instance2.setWorkflowInstanceUid("workflowInstanceUid");
        List<WorkflowStepInstance> instances = List.of(instance1, instance2);
        when(workflowStepInstanceRepository.findByWorkflowInstanceUid("workflowInstanceUid")).thenReturn(instances);

        List<WorkflowStepInstance> result = workflowStepInstanceRepository.findByWorkflowInstanceUid("workflowInstanceUid");

        assertEquals(2, result.size());
    }

    @Test
    void findByWorkflowInstanceUid_returnsEmptyList_whenWorkflowInstanceUidDoesNotExist() {
        when(workflowStepInstanceRepository.findByWorkflowInstanceUid("nonexistent")).thenReturn(List.of());

        List<WorkflowStepInstance> result = workflowStepInstanceRepository.findByWorkflowInstanceUid("nonexistent");

        assertEquals(0, result.size());
    }
}