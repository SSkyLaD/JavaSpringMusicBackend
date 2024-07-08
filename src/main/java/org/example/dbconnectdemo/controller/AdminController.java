package org.example.dbconnectdemo.controller;

import lombok.AllArgsConstructor;
import org.example.dbconnectdemo.dto.ResponseDataList;
import org.example.dbconnectdemo.dto.ResponseMessage;
import org.example.dbconnectdemo.dto.SongDto;
import org.example.dbconnectdemo.dto.UserDto;
import org.example.dbconnectdemo.model.User;
import org.example.dbconnectdemo.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5150")
@RequestMapping("/api/v1/admin")
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    private ResponseEntity<Object> getAllUsersDetail(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            List<UserDto> users = adminService.getAllUsersDetail(username);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success",users.size(),users));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    private ResponseEntity<Object> deleteUser(@PathVariable Long id, @RequestBody Map<String,String> password){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            String deletedUsername = adminService.deleteUser(username, password.get("password"),id);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("User " + deletedUsername + " delete successfully!"));
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @GetMapping("/users/{id}/songs")
    private ResponseEntity<Object> getUserSongs(@PathVariable("id") Long userId){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            List<SongDto> data = adminService.getUserSongs(username,userId);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!", data.size(), data));
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}/songs/{songId}")
    private ResponseEntity<Object> deleteUserSong(@PathVariable("userId") Long userId, @PathVariable("songId") Long songId){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            SongDto song = adminService.deleteUserSong(username, userId,songId);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("Song id: " + song.getId() + " - " + song.getName() + " - " + song.getArtist() + " delete successfully!"));
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }
}
