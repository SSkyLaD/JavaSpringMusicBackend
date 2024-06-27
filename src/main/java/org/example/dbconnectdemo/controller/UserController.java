package org.example.dbconnectdemo.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.dbconnectdemo.exception.ResourceNotFoundException;
import org.example.dbconnectdemo.dto.ResponseData;
import org.example.dbconnectdemo.model.User;
import org.example.dbconnectdemo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    private ResponseEntity<ResponseData> getUserData(){
        @lombok.Data
        @AllArgsConstructor
        @NoArgsConstructor
        class Data {
            private String username;
            private String email;
            private Date accountCreateDate;
            private int numberOfSong;
            private double availableMemory;
        }
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getUserData(username);
            Data resData = new Data(user.getUsername(),user.getEmail(),user.getCreateDate(),user.getSumOfSongs(),user.getAvailableMemory());
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData("Success",resData));
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData(e.getMessage()));
        }
    }

    //Ổn nếu có Frontend xóa token và logout khi xóa.
    @DeleteMapping
    private ResponseEntity<ResponseData> deleteUser(@RequestBody Map<String,String> password){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            userService.deleteUser(username,password.get("password"));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData("User " + username + " delete successfully!"));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData(e.getMessage()));
        }
    }

    @GetMapping("/songs")
    private ResponseEntity<ResponseData> getUserSongs(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData("Success!", userService.getUserSong(username)));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData(e.getMessage()));
        }
    }
}