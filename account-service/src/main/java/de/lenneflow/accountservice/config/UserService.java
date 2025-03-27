package de.lenneflow.accountservice.config;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.lenneflow.accountservice.enums.Role;
import de.lenneflow.accountservice.model.User;
import de.lenneflow.accountservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    public static final String MASTER_CLIENT_USER_NAME = "masterClient";

    final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userName)
            throws UsernameNotFoundException {

        Optional<User> opt = Optional.ofNullable(findByUsername(userName));

        if (opt.isEmpty())
            throw new UsernameNotFoundException("User with username: " + userName + " not found !");
        else {
            User user = opt.get();
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    user.getAuthorities()
                            .stream()
                            .map(role -> new SimpleGrantedAuthority(role.getValue()))
                            .collect(Collectors.toSet())
            );
        }

    }

    public User findByUsername(String username){
        if(username != null && username.equals(MASTER_CLIENT_USER_NAME)){
            return getMasterClientUser();
        }
        return userRepository.findByUsername(username);
    }

    public User findByEmail(String email){
        if (email != null && email.equals(MASTER_CLIENT_USER_NAME)){
            return getMasterClientUser();
        }
        return userRepository.findByEmail(email);
    }

    public User findByUid(String uid){
        if(uid != null && uid.equals(MASTER_CLIENT_USER_NAME)){
            return getMasterClientUser();
        }
        return userRepository.findByUid(uid);
    }

    public User save(User user){
        return userRepository.save(user);
    }

    public void delete(User user){
        userRepository.delete(user);
    }

    public List<User> findAll(){
        return userRepository.findAll();
    }


    private User getMasterClientUser(){
        User user = new User();
        user.setAuthorities(Collections.singleton(Role.ROLE_ADMIN));
        user.setEmail(MASTER_CLIENT_USER_NAME);
        user.setPassword("$2a$10$EcIEI3PBEjc5Ft6bu7jfi.WZF.2cejm5XaNafaFzLldTH8VO7LbrC");
        user.setUsername(MASTER_CLIENT_USER_NAME);
        user.setLocked(false);
        user.setEnabled(true);
        user.setExpired(false);
        user.setUid("MASTER_CLIENT_USER_NAME");
        return user;
    }
}