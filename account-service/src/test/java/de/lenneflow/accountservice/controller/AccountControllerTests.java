package de.lenneflow.accountservice.controller;

import de.lenneflow.accountservice.config.JwtService;
import de.lenneflow.accountservice.dto.LoginDTO;
import de.lenneflow.accountservice.dto.LoginResponse;
import de.lenneflow.accountservice.dto.UserDto;
import de.lenneflow.accountservice.enums.Role;
import de.lenneflow.accountservice.exception.PayloadNotValidException;
import de.lenneflow.accountservice.model.User;
import de.lenneflow.accountservice.repository.UserRepository;
import de.lenneflow.accountservice.util.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class AccountControllerTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Validator validator;

    @Mock
    private AuthenticationProvider authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserById_returnsUser_whenUserExists() {
        User user = new User();
        user.setUid("123");
        when(userRepository.findByUid("123")).thenReturn(user);

        User result = accountController.getUserById("123");

        assertEquals("123", result.getUid());
    }

    @Test
    void getUserById_returnsNull_whenUserDoesNotExist() {
        when(userRepository.findByUid("123")).thenReturn(null);

        User result = accountController.getUserById("123");

        assertNull(result);
    }

    @Test
    void registerUser_savesAndReturnsUser_whenValidUserDto() {
        UserDto userDto = new UserDto();
        userDto.setPassword("password");
        userDto.setAuthorities(Set.of(Role.ROLE_USER));
        userDto.setUsername("user");
        userDto.setEmail("email");
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).then(AdditionalAnswers.returnsFirstArg());

        User result = accountController.registerUser(userDto);

        assertNotNull(result.getUid());
        assertEquals("encodedPassword", result.getPassword());
    }

    @Test
    void login_returnsLoginResponse_whenCredentialsAreValid() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("user");
        loginDTO.setPassword("password");
        User user = new User();
        user.setUsername("user");
        when(userRepository.findByUsername("user")).thenReturn(user);
        when(jwtService.generateToken(any(), any())).thenReturn("jwtToken");

        ResponseEntity<LoginResponse> response = accountController.login(loginDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getAccessToken());
    }

    @Test
    void login_throwsException_whenCredentialsAreInvalid() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("user");
        loginDTO.setPassword("wrongPassword");
        doThrow(new RuntimeException()).when(authenticationManager).authenticate(any());

        assertThrows(PayloadNotValidException.class, () -> accountController.login(loginDTO));
    }
}