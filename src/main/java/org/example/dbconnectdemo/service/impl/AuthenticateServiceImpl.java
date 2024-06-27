package org.example.dbconnectdemo.service.impl;

import lombok.AllArgsConstructor;
import org.example.dbconnectdemo.dto.UserDto;
import org.example.dbconnectdemo.exception.InvalidInputException;
import org.example.dbconnectdemo.model.Role;
import org.example.dbconnectdemo.repository.UserRepository;
import org.example.dbconnectdemo.service.AuthenticateService;
import org.example.dbconnectdemo.service.UserService;
import org.example.dbconnectdemo.spring_security.JwtUlti;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticateServiceImpl implements AuthenticateService {

    private final UserService userService;

    private final JwtUlti jwtUlti;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @Override
    public void register(UserDto userDto) {
        userDto.setRole(Role.USER); //Hardcode every created account is user
        userService.createUser(userDto);
    }

    @Override
    public String login(UserDto userDto) {
        if(userDto.getUsername() == null || userDto.getUsername().isEmpty()){
            throw new InvalidInputException("Username cannot be blank");
        }
        if(userDto.getPassword() == null || userDto.getPassword().isEmpty()){
            throw new InvalidInputException("Password cannot be blank");
        }

        //Validate username and password throw AuthenticationException
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userDto.getUsername(),
                        userDto.getPassword()
                )
        );
        UserDetails user = userRepository.findByUsername(userDto.getUsername())
                .orElseThrow();
        return jwtUlti.generateToken(user);
    }
}
