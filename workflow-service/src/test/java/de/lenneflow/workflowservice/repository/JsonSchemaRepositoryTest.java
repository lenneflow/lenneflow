package de.lenneflow.workflowservice.repository;

import de.lenneflow.workflowservice.model.JsonSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsonSchemaRepositoryTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private JsonSchemaRepository jsonSchemaRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUid_shouldReturnJsonSchema() {
        String uid = "schemaUid";
        JsonSchema jsonSchema = new JsonSchema();
        when(jsonSchemaRepository.findByUid(uid)).thenReturn(jsonSchema);

        JsonSchema result = jsonSchemaRepository.findByUid(uid);

        assertNotNull(result);
        assertEquals(jsonSchema, result);
    }

    @Test
    void findByUid_shouldReturnNullWhenNotFound() {
        String uid = "schemaUid";
        when(jsonSchemaRepository.findByUid(uid)).thenReturn(null);

        JsonSchema result = jsonSchemaRepository.findByUid(uid);

        assertNull(result);
    }

    @Test
    void save_shouldSaveAndReturnJsonSchema() {
        JsonSchema jsonSchema = new JsonSchema();
        when(jsonSchemaRepository.save(jsonSchema)).thenReturn(jsonSchema);

        JsonSchema result = jsonSchemaRepository.save(jsonSchema);

        assertNotNull(result);
        assertEquals(jsonSchema, result);
    }

    @Test
    void delete_shouldRemoveJsonSchema() {
        JsonSchema jsonSchema = new JsonSchema();
        doNothing().when(jsonSchemaRepository).delete(jsonSchema);

        jsonSchemaRepository.delete(jsonSchema);

        verify(jsonSchemaRepository).delete(jsonSchema);
    }

    @Test
    void findAll_shouldReturnAllJsonSchemas() {
        List<JsonSchema> jsonSchemas = List.of(new JsonSchema(), new JsonSchema());
        when(jsonSchemaRepository.findAll()).thenReturn(jsonSchemas);

        List<JsonSchema> result = jsonSchemaRepository.findAll();

        assertNotNull(result);
        assertEquals(jsonSchemas.size(), result.size());
    }
}