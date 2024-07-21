package org.example.dbconnectdemo.controller;

import lombok.RequiredArgsConstructor;
import org.example.dbconnectdemo.dto.RegisterBody;
import org.example.dbconnectdemo.dto.ResponseData;
import org.example.dbconnectdemo.dto.UserDto;
import org.example.dbconnectdemo.exception.InvalidInputException;
import org.example.dbconnectdemo.exception.UsernameAlreadyExistException;
import org.example.dbconnectdemo.dto.ResponseMessage;
import org.example.dbconnectdemo.service.AuthenticateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5150")
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final AuthenticateService authenticateService;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterBody registerBody) {
        try {
            authenticateService.register(registerBody);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseMessage("Account created successfully!"));
        } catch (InvalidInputException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        } catch(UsernameAlreadyExistException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMessage(e.getMessage()));
        }
    }
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserDto userDto){
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        } catch (AuthenticationException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage("Username or password not correct!"));
        }
    }
}
