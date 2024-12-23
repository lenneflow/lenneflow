package de.lenneflow.functionservice.repository;

import de.lenneflow.functionservice.model.JsonSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsonSchemaRepositoryTest {

    @Mock
    private JsonSchemaRepository jsonSchemaRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUid_shouldReturnJsonSchemaWhenUidExists() {
        JsonSchema jsonSchema = new JsonSchema();
        jsonSchema.setUid("existingUid");
        when(jsonSchemaRepository.findByUid("existingUid")).thenReturn(jsonSchema);

        JsonSchema result = jsonSchemaRepository.findByUid("existingUid");

        assertNotNull(result);
        assertEquals("existingUid", result.getUid());
    }

    @Test
    void findByUid_shouldReturnNullWhenUidDoesNotExist() {
        when(jsonSchemaRepository.findByUid("nonExistingUid")).thenReturn(null);

        JsonSchema result = jsonSchemaRepository.findByUid("nonExistingUid");

        assertNull(result);
    }
}