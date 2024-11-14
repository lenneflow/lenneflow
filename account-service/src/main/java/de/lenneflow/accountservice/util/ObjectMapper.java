package de.lenneflow.accountservice.util;

import de.lenneflow.accountservice.dto.UserDto;
import de.lenneflow.accountservice.model.User;

public class ObjectMapper {

    private ObjectMapper() {}

    public static User mapToUser(UserDto userDto) {
        User user = new User();
        user.setAuthorities(userDto.getAuthorities());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setUsername(userDto.getUsername());
        return user;
    }
}
