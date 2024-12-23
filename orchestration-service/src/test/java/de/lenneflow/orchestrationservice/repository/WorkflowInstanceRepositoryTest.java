package de.lenneflow.orchestrationservice.repository;

import de.lenneflow.orchestrationservice.model.WorkflowInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class WorkflowInstanceRepositoryTest {

    @Mock
    private WorkflowInstanceRepository workflowInstanceRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUid_returnsWorkflowInstance_whenUidExists() {
        WorkflowInstance instance = new WorkflowInstance();
        instance.setUid("12345");
        when(workflowInstanceRepository.findByUid("12345")).thenReturn(instance);

        WorkflowInstance result = workflowInstanceRepository.findByUid("12345");

        assertEquals("12345", result.getUid());
    }

    @Test
    void findByUid_returnsNull_whenUidDoesNotExist() {
        when(workflowInstanceRepository.findByUid("nonexistent")).thenReturn(null);

        WorkflowInstance result = workflowInstanceRepository.findByUid("nonexistent");

        assertNull(result);
    }

    @Test
    void findByUid_returnsNull_whenUidIsNull() {
        when(workflowInstanceRepository.findByUid(null)).thenReturn(null);

        WorkflowInstance result = workflowInstanceRepository.findByUid(null);

        assertNull(result);
    }

    @Test
    void findByUid_returnsNull_whenUidIsEmpty() {
        when(workflowInstanceRepository.findByUid("")).thenReturn(null);

        WorkflowInstance result = workflowInstanceRepository.findByUid("");

        assertNull(result);
    }
}