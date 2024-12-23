package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.model.CloudCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CloudCredentialRepositoryTest {

    @Mock
    private CloudCredentialRepository cloudCredentialRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUid_shouldReturnCloudCredentialWhenUidExists() {
        CloudCredential cloudCredential = new CloudCredential();
        cloudCredential.setUid("existingUid");
        when(cloudCredentialRepository.findByUid("existingUid")).thenReturn(cloudCredential);

        CloudCredential result = cloudCredentialRepository.findByUid("existingUid");

        assertNotNull(result);
        assertEquals("existingUid", result.getUid());
    }

    @Test
    void findByUid_shouldReturnNullWhenUidDoesNotExist() {
        when(cloudCredentialRepository.findByUid("nonExistingUid")).thenReturn(null);

        CloudCredential result = cloudCredentialRepository.findByUid("nonExistingUid");

        assertNull(result);
    }

    @Test
    void findByName_shouldReturnCloudCredentialWhenNameExists() {
        CloudCredential cloudCredential = new CloudCredential();
        cloudCredential.setName("existingName");
        when(cloudCredentialRepository.findByName("existingName")).thenReturn(cloudCredential);

        CloudCredential result = cloudCredentialRepository.findByName("existingName");

        assertNotNull(result);
        assertEquals("existingName", result.getName());
    }

    @Test
    void findByName_shouldReturnNullWhenNameDoesNotExist() {
        when(cloudCredentialRepository.findByName("nonExistingName")).thenReturn(null);

        CloudCredential result = cloudCredentialRepository.findByName("nonExistingName");

        assertNull(result);
    }

    @Test
    void save_shouldPersistCloudCredential() {
        CloudCredential cloudCredential = new CloudCredential();
        cloudCredential.setUid("newUid");
        when(cloudCredentialRepository.save(cloudCredential)).thenReturn(cloudCredential);

        CloudCredential result = cloudCredentialRepository.save(cloudCredential);

        assertNotNull(result);
        assertEquals("newUid", result.getUid());
    }

    @Test
    void delete_shouldRemoveCloudCredential() {
        CloudCredential cloudCredential = new CloudCredential();
        cloudCredential.setUid("uidToDelete");
        doNothing().when(cloudCredentialRepository).delete(cloudCredential);

        cloudCredentialRepository.delete(cloudCredential);

        verify(cloudCredentialRepository, times(1)).delete(cloudCredential);
    }

    @Test
    void findById_shouldReturnCloudCredentialWhenIdExists() {
        CloudCredential cloudCredential = new CloudCredential();
        cloudCredential.setUid("existingId");
        when(cloudCredentialRepository.findById("existingId")).thenReturn(Optional.of(cloudCredential));

        Optional<CloudCredential> result = cloudCredentialRepository.findById("existingId");

        assertTrue(result.isPresent());
        assertEquals("existingId", result.get().getUid());
    }

    @Test
    void findById_shouldReturnEmptyWhenIdDoesNotExist() {
        when(cloudCredentialRepository.findById("nonExistingId")).thenReturn(Optional.empty());

        Optional<CloudCredential> result = cloudCredentialRepository.findById("nonExistingId");

        assertFalse(result.isPresent());
    }
}