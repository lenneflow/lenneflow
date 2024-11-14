package de.lenneflow.accountservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.lenneflow.accountservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class User implements UserDetails {

    @Id
    private String uid;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    @JsonIgnore
    private String password;

    private Set<Role> authorities;

    private boolean expired;

    private boolean enabled;

    private boolean locked;
}
