package de.lenneflow.accountservice.controller;

import de.lenneflow.accountservice.dto.UserDto;
import de.lenneflow.accountservice.model.User;
import de.lenneflow.accountservice.repository.UserRepository;
import de.lenneflow.accountservice.util.ObjectMapper;
import de.lenneflow.accountservice.util.Validator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts API")
@RequiredArgsConstructor
public class AccountController {

    final UserRepository userRepository;
    final Validator validator;

    @Operation(summary = "Get a User by uid")
    @GetMapping("/user/{uid}")
    public User getUserById(@PathVariable("uid") String uid) {
        return userRepository.findByUid(uid);
    }

    @Operation(summary = "Register a new User")
    @GetMapping("/user/register")
    public User registerUser(@RequestBody UserDto userDto) {
        validator.validate(userDto);
        User user = ObjectMapper.mapToUser(userDto);
        user.setUid(UUID.randomUUID().toString());
        user.setLocked(false);
        user.setEnabled(true);
        user.setExpired(false);
        return userRepository.save(user);
    }
}
