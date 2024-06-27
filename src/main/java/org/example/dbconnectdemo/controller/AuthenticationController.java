package org.example.dbconnectdemo.controller;

import lombok.RequiredArgsConstructor;
import org.example.dbconnectdemo.dto.UserDto;
import org.example.dbconnectdemo.exception.InvalidInputException;
import org.example.dbconnectdemo.exception.UsernameAlreadyExistException;
import org.example.dbconnectdemo.dto.ResponseData;
import org.example.dbconnectdemo.service.AuthenticateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final AuthenticateService authenticateService;

    @PostMapping("/register")
    public ResponseEntity<ResponseData> register(@RequestBody UserDto userdto) {
        try {
            authenticateService.register(userdto);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseData("Account created successfully!"));
        } catch (InvalidInputException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData(e.getMessage()));
        } catch(UsernameAlreadyExistException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseData(e.getMessage()));
        }
    }
    @PostMapping("/login")
    public ResponseEntity<ResponseData> login(@RequestBody UserDto userDto){
        @lombok.Data
        @RequiredArgsConstructor
        class Data {
            private String token;
            private String username;

            public Data(String token, String username){
                this.token = token;
                this.username = username;
            }
        }
        try {
            String token = authenticateService.login(userDto);
            Data responseLogin = new Data(token, userDto.getUsername());
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData("Login successfully!",responseLogin));
        } catch (InvalidInputException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData(e.getMessage()));
        } catch (AuthenticationException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData("Username or password not correct!"));
        }
    }
}
