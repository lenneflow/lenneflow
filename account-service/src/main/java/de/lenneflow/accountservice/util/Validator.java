package de.lenneflow.accountservice.util;

import de.lenneflow.accountservice.dto.UserDto;
import de.lenneflow.accountservice.exception.PayloadNotValidException;
import de.lenneflow.accountservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Validator {

    final UserRepository userRepository;

    public void validate(UserDto userDto){
        if(userDto == null){
            throw new PayloadNotValidException("Payload is null");
        }
        if(userDto.getUsername() == null || userDto.getUsername().isEmpty()){
            throw new PayloadNotValidException("Username is empty");
        }
        if(userDto.getPassword() == null || userDto.getPassword().isEmpty()){
            throw new PayloadNotValidException("Password is empty");
        }
        if(userDto.getEmail() == null || userDto.getEmail().isEmpty()){
            throw new PayloadNotValidException("Email is empty");
        }
        if(userRepository.findByUid(userDto.getUid()) != null){
            throw new PayloadNotValidException("User already exists");
        }
        if(userRepository.findByEmail(userDto.getEmail()) != null){
            throw new PayloadNotValidException("User already exists");
        }
        if(userRepository.findByUsername(userDto.getUsername()) != null){
            throw new PayloadNotValidException("User already exists");
        }

    }
}
