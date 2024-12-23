package de.lenneflow.accountservice.util;

import de.lenneflow.accountservice.dto.UserDto;
import de.lenneflow.accountservice.enums.Role;
import de.lenneflow.accountservice.model.User;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ObjectMapperTest {

    @Test
    void mapToUser_returnsUserWithCorrectFields_whenUserDtoIsValid() {
        UserDto userDto = new UserDto();
        userDto.setAuthorities(Set.of(Role.ROLE_USER));
        userDto.setEmail("user@example.com");
        userDto.setPassword("password");
        userDto.setUsername("username");

        User user = ObjectMapper.mapToUser(userDto);

        assertTrue(user.getAuthorities().contains(Role.ROLE_USER));
        assertEquals("user@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals("username", user.getUsername());
    }

    @Test
    void mapToUser_returnsUserWithNullFields_whenUserDtoFieldsAreNull() {
        UserDto userDto = new UserDto();
        userDto.setAuthorities(null);
        userDto.setEmail(null);
        userDto.setPassword(null);
        userDto.setUsername(null);

        User user = ObjectMapper.mapToUser(userDto);

        assertNull(user.getAuthorities());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getUsername());
    }
}