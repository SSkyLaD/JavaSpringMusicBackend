package org.example.dbconnectdemo.controller;

import lombok.AllArgsConstructor;
import org.example.dbconnectdemo.dto.ResponseDataList;
import org.example.dbconnectdemo.dto.ResponseMessage;
import org.example.dbconnectdemo.dto.SongDto;
import org.example.dbconnectdemo.dto.UserDto;
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

    // Allow sort by username, email, createDate, songs, memory, asc or desc
    // /api/v1/admin/users?field=memory&direction=desc
    @GetMapping("/users")
    private ResponseEntity<Object> getAllUsersDetail(@RequestParam(required = false) Map<String,String> qparam){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            if(!qparam.isEmpty()){
                List<UserDto> users = adminService.getAllUserDetailWithSort(username,qparam.get("field"), qparam.get("direction"));
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!",users.size(),users));
            }
            List<UserDto> users = adminService.getAllUsersDetail(username);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!",users.size(),users));
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

    // Allow Sort by Name, Artist, Duration, Size, uploadDate ASC and DESC
    // /api/v1/admin/users/1/songs?field=uploadDate&direction=desc
    @GetMapping("/users/{id}/songs")
    private ResponseEntity<Object> getUserSongs(@PathVariable("id") Long userId,@RequestParam(required = false) Map<String,String> qparam){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            if(!qparam.isEmpty()){
                List<SongDto> data = adminService.getAllUserSongsWithSort(username,userId,qparam.get("field"), qparam.get("direction"));
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!",data.size(),data));
            }
            List<SongDto> data = adminService.getAllUserSongs(username,userId);
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
