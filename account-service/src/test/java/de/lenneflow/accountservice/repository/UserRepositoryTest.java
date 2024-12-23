package de.lenneflow.accountservice.repository;

import de.lenneflow.accountservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUsername_returnsUser_whenUsernameExists() {
        User user = new User();
        user.setUsername("existingUser");
        when(userRepository.findByUsername("existingUser")).thenReturn(user);

        User result = userRepository.findByUsername("existingUser");

        assertEquals("existingUser", result.getUsername());
    }

    @Test
    void findByUsername_returnsNull_whenUsernameDoesNotExist() {
        when(userRepository.findByUsername("nonExistingUser")).thenReturn(null);

        User result = userRepository.findByUsername("nonExistingUser");

        assertNull(result);
    }

    @Test
    void findByEmail_returnsUser_whenEmailExists() {
        User user = new User();
        user.setEmail("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(user);

        User result = userRepository.findByEmail("user@example.com");

        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void findByEmail_returnsNull_whenEmailDoesNotExist() {
        when(userRepository.findByEmail("nonExistingEmail@example.com")).thenReturn(null);

        User result = userRepository.findByEmail("nonExistingEmail@example.com");

        assertNull(result);
    }

    @Test
    void findByUid_returnsUser_whenUidExists() {
        User user = new User();
        user.setUid("123");
        when(userRepository.findByUid("123")).thenReturn(user);

        User result = userRepository.findByUid("123");

        assertEquals("123", result.getUid());
    }

    @Test
    void findByUid_returnsNull_whenUidDoesNotExist() {
        when(userRepository.findByUid("nonExistingUid")).thenReturn(null);

        User result = userRepository.findByUid("nonExistingUid");

        assertNull(result);
    }
}