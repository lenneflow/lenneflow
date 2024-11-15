package de.lenneflow.accountservice.config;

import java.util.Optional;
import java.util.stream.Collectors;

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

    final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String userName)
            throws UsernameNotFoundException {

        Optional<User> opt = Optional.ofNullable(userRepo.findByUsername(userName));

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


}