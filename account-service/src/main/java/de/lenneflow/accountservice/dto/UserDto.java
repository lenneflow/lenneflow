package de.lenneflow.accountservice.dto;

import de.lenneflow.accountservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto implements UserDetails {

    private String firstname;

    private String lastname;

    private String username;

    private String email;

    private String password;

    private Set<Role> authorities;

}
