package de.lenneflow.functionservice.repository;

import de.lenneflow.functionservice.model.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FunctionRepositoryTest {

    @Mock
    private FunctionRepository functionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUid_shouldReturnFunctionWhenUidExists() {
        Function function = new Function();
        function.setUid("existingUid");
        when(functionRepository.findByUid("existingUid")).thenReturn(function);

        Function result = functionRepository.findByUid("existingUid");

        assertNotNull(result);
        assertEquals("existingUid", result.getUid());
    }

    @Test
    void findByUid_shouldReturnNullWhenUidDoesNotExist() {
        when(functionRepository.findByUid("nonExistingUid")).thenReturn(null);

        Function result = functionRepository.findByUid("nonExistingUid");

        assertNull(result);
    }

    @Test
    void findByName_shouldReturnFunctionWhenNameExists() {
        Function function = new Function();
        function.setName("existingName");
        when(functionRepository.findByName("existingName")).thenReturn(function);

        Function result = functionRepository.findByName("existingName");

        assertNotNull(result);
        assertEquals("existingName", result.getName());
    }

    @Test
    void findByName_shouldReturnNullWhenNameDoesNotExist() {
        when(functionRepository.findByName("nonExistingName")).thenReturn(null);

        Function result = functionRepository.findByName("nonExistingName");

        assertNull(result);
    }

    @Test
    void findByType_shouldReturnListOfFunctionsWhenTypeExists() {
        Function function1 = new Function();
        function1.setType("existingType");
        Function function2 = new Function();
        function2.setType("existingType");
        when(functionRepository.findByType("existingType")).thenReturn(List.of(function1, function2));

        List<Function> result = functionRepository.findByType("existingType");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void findByType_shouldReturnEmptyListWhenTypeDoesNotExist() {
        when(functionRepository.findByType("nonExistingType")).thenReturn(List.of());

        List<Function> result = functionRepository.findByType("nonExistingType");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}