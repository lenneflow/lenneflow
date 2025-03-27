package de.lenneflow.accountservice.controller;

import de.lenneflow.accountservice.config.JwtService;
import de.lenneflow.accountservice.config.UserService;
import de.lenneflow.accountservice.dto.LoginDTO;
import de.lenneflow.accountservice.dto.LoginResponse;
import de.lenneflow.accountservice.dto.UserDto;
import de.lenneflow.accountservice.exception.PayloadNotValidException;
import de.lenneflow.accountservice.model.User;
import de.lenneflow.accountservice.util.ObjectMapper;
import de.lenneflow.accountservice.util.Validator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts API")
@RequiredArgsConstructor
public class AccountController {

    @Value("${application.security.jwt.expiration}")
    private long expiration;

    private static final String TOKEN_TYPE = "Bearer";

    final UserService userRepository;
    final Validator validator;
    final AuthenticationProvider authenticationManager;
    final JwtService jwtService;
    final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Get a User by uid")
    @GetMapping("/secure/user/{uid}")
    public User getUserById(@PathVariable("uid") String uid) {
        return userRepository.findByUid(uid);
    }

    @Operation(summary = "Get a User by User Name")
    @GetMapping("/secure/user/name/{userName}")
    public User getUserByName(@PathVariable("userName") String userName) {
        return userRepository.findByUsername(userName);
    }

    @Operation(summary = "Create a new User")
    @PostMapping("/secure/user/create")
    public User registerUser(@RequestBody UserDto userDto) {
        validator.validate(userDto);
        User user = ObjectMapper.mapToUser(userDto);
        user.setUid(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setLocked(false);
        user.setEnabled(true);
        user.setExpired(false);
        return userRepository.save(user);
    }

    @DeleteMapping("/secure/user/{uid}")
    public void deleteUser(@PathVariable String uid) {
        User user = userRepository.findByUid(uid);
        userRepository.delete(user);
    }

    @GetMapping("/secure/user/list")
    public List<User> userList() {
        return userRepository.findAll();
    }

    @PostMapping("/user/token")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginDTO loginDTO) {
        try {
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
            User user = userRepository.findByUsername(loginDTO.getUsername());
            Date tokenExpiration = new Date(System.currentTimeMillis() + expiration);
            String jwt = jwtService.generateToken(user, tokenExpiration);
            LoginResponse loginResponse = LoginResponse
                    .builder()
                    .accessToken(jwt)
                    .tokenType(TOKEN_TYPE)
                    .expiration(tokenExpiration.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime())
                    .build();
            return new ResponseEntity<>(loginResponse, HttpStatus.OK);
        }
        catch (Exception e)
        {
            throw new PayloadNotValidException("Could not validate login");
        }
    }
}
