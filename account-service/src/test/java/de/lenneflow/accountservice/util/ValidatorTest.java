package de.lenneflow.accountservice.util;

import de.lenneflow.accountservice.dto.UserDto;
import de.lenneflow.accountservice.exception.PayloadNotValidException;
import de.lenneflow.accountservice.model.User;
import de.lenneflow.accountservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ValidatorTest {

    private Validator validator;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        validator = new Validator(userRepository);
    }

    @Test
    void validateThrowsExceptionWhenPayloadIsNull() {
        assertThrows(PayloadNotValidException.class, () -> validator.validate(null));
    }

    @Test
    void validateThrowsExceptionWhenUsernameIsEmpty() {
        UserDto userDto = new UserDto();
        userDto.setUsername("");
        userDto.setPassword("password");
        userDto.setEmail("email@example.com");

        assertThrows(PayloadNotValidException.class, () -> validator.validate(userDto));
    }

    @Test
    void validateThrowsExceptionWhenPasswordIsEmpty() {
        UserDto userDto = new UserDto();
        userDto.setUsername("username");
        userDto.setPassword("");
        userDto.setEmail("email@example.com");

        assertThrows(PayloadNotValidException.class, () -> validator.validate(userDto));
    }

    @Test
    void validateThrowsExceptionWhenEmailIsEmpty() {
        UserDto userDto = new UserDto();
        userDto.setUsername("username");
        userDto.setPassword("password");
        userDto.setEmail("");

        assertThrows(PayloadNotValidException.class, () -> validator.validate(userDto));
    }

    @Test
    void validateThrowsExceptionWhenUserAlreadyExistsByEmail() {
        UserDto userDto = new UserDto();
        userDto.setUsername("username");
        userDto.setPassword("password");
        userDto.setEmail("email@example.com");

        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(new User());

        assertThrows(PayloadNotValidException.class, () -> validator.validate(userDto));
    }

    @Test
    void validateThrowsExceptionWhenUserAlreadyExistsByUsername() {
        UserDto userDto = new UserDto();
        userDto.setUsername("username");
        userDto.setPassword("password");
        userDto.setEmail("email@example.com");

        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(new User());

        assertThrows(PayloadNotValidException.class, () -> validator.validate(userDto));
    }

    @Test
    void validateSucceedsWhenAllFieldsAreValid() {
        UserDto userDto = new UserDto();
        userDto.setUsername("username");
        userDto.setPassword("password");
        userDto.setEmail("email@example.com");

        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(null);
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(null);

        validator.validate(userDto);
    }
}