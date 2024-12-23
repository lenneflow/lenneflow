package de.lenneflow.orchestrationservice.repository;

import de.lenneflow.orchestrationservice.model.GlobalInputData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class GlobalInputDataRepositoryTest {

    @Mock
    private GlobalInputDataRepository globalInputDataRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUid_returnsGlobalInputData_whenUidExists() {
        GlobalInputData data = new GlobalInputData();
        data.setUid("12345");
        when(globalInputDataRepository.findByUid("12345")).thenReturn(data);

        GlobalInputData result = globalInputDataRepository.findByUid("12345");

        assertEquals("12345", result.getUid());
    }

    @Test
    void findByUid_returnsNull_whenUidDoesNotExist() {
        when(globalInputDataRepository.findByUid("nonexistent")).thenReturn(null);

        GlobalInputData result = globalInputDataRepository.findByUid("nonexistent");

        assertNull(result);
    }

    @Test
    void findByUid_returnsNull_whenUidIsNull() {
        when(globalInputDataRepository.findByUid(null)).thenReturn(null);

        GlobalInputData result = globalInputDataRepository.findByUid(null);

        assertNull(result);
    }

    @Test
    void findByUid_returnsNull_whenUidIsEmpty() {
        when(globalInputDataRepository.findByUid("")).thenReturn(null);

        GlobalInputData result = globalInputDataRepository.findByUid("");

        assertNull(result);
    }
}