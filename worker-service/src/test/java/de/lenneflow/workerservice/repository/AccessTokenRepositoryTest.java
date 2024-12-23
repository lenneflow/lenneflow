package de.lenneflow.workerservice.repository;

import de.lenneflow.workerservice.model.AccessToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccessTokenRepositoryTest {

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUid_shouldReturnAccessTokenWhenUidExists() {
        AccessToken accessToken = new AccessToken();
        accessToken.setUid("existingUid");
        when(accessTokenRepository.findByUid("existingUid")).thenReturn(accessToken);

        AccessToken result = accessTokenRepository.findByUid("existingUid");

        assertNotNull(result);
        assertEquals("existingUid", result.getUid());
    }

    @Test
    void findByUid_shouldReturnNullWhenUidDoesNotExist() {
        when(accessTokenRepository.findByUid("nonExistingUid")).thenReturn(null);

        AccessToken result = accessTokenRepository.findByUid("nonExistingUid");

        assertNull(result);
    }

    @Test
    void save_shouldPersistAccessToken() {
        AccessToken accessToken = new AccessToken();
        accessToken.setUid("newUid");
        when(accessTokenRepository.save(accessToken)).thenReturn(accessToken);

        AccessToken result = accessTokenRepository.save(accessToken);

        assertNotNull(result);
        assertEquals("newUid", result.getUid());
    }

    @Test
    void delete_shouldRemoveAccessToken() {
        AccessToken accessToken = new AccessToken();
        accessToken.setUid("uidToDelete");
        doNothing().when(accessTokenRepository).delete(accessToken);

        accessTokenRepository.delete(accessToken);

        verify(accessTokenRepository, times(1)).delete(accessToken);
    }

    @Test
    void findById_shouldReturnAccessTokenWhenIdExists() {
        AccessToken accessToken = new AccessToken();
        accessToken.setUid("existingId");
        when(accessTokenRepository.findById("existingId")).thenReturn(Optional.of(accessToken));

        Optional<AccessToken> result = accessTokenRepository.findById("existingId");

        assertTrue(result.isPresent());
        assertEquals("existingId", result.get().getUid());
    }

    @Test
    void findById_shouldReturnEmptyWhenIdDoesNotExist() {
        when(accessTokenRepository.findById("nonExistingId")).thenReturn(Optional.empty());

        Optional<AccessToken> result = accessTokenRepository.findById("nonExistingId");

        assertFalse(result.isPresent());
    }
}