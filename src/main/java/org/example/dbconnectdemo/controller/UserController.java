package org.example.dbconnectdemo.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.dbconnectdemo.dto.ResponseData;
import org.example.dbconnectdemo.dto.ResponseDataList;
import org.example.dbconnectdemo.dto.SongDto;
import org.example.dbconnectdemo.exception.ResourceNotFoundException;
import org.example.dbconnectdemo.dto.ResponseMessage;
import org.example.dbconnectdemo.model.Song;
import org.example.dbconnectdemo.model.User;
import org.example.dbconnectdemo.repository.UserRepository;
import org.example.dbconnectdemo.service.UserService;
import org.example.dbconnectdemo.spring_security.JwtUlti;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:5150")
@RequestMapping("/api/v1/users")
public class UserController {

    private final JwtUlti jwtUlti;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> maxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage() + " (200MB)"));
    }

    private final UserService userService;

    @GetMapping
    private ResponseEntity<Object> getUserData() {
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
            Data resData = new Data(user.getUsername(), user.getEmail(), user.getCreateDate(), user.getUserStorageList().getSumOfSongs(), user.getUserStorageList().getAvailableMemory() / (1024 * 1024));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData("Success",resData));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    //Ổn nếu có Frontend xóa token và logout khi xóa.
    @DeleteMapping
    private ResponseEntity<Object> deleteUser(@RequestBody Map<String, String> password) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            userService.deleteUser(username, password.get("password"));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("User " + username + " delete successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @GetMapping("/songs")
    private ResponseEntity<Object> getUserSongs() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            List<SongDto> data = userService.getUserSongs(username);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!", data.size(), data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }



    @DeleteMapping("/songs/{id}")
    private ResponseEntity<Object> deleteUserSong(@PathVariable("id") Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            SongDto song = userService.deleteSongFromUser(username, id);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("Song id: " + song.getId() + " - " + song.getName() + " - " + song.getArtist() + " delete successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @GetMapping("/songs/favorites")
    private ResponseEntity<Object> getUserFavorites() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            List<SongDto> data = userService.getUserFavoriteSongs(username);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!", data.size(), data));
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @PatchMapping("/songs/favorites/{id}")
    private ResponseEntity<Object> addUserFavorites(@PathVariable("id") Long id,@RequestBody Map<String, Boolean> isFavorite) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            System.out.println(isFavorite);
            System.out.println(isFavorite.get("isFavorite"));
            SongDto song = userService.updateUserFavoriteSong(username,id,isFavorite.get("isFavorite"));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData("success", song));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    //Testing
    @GetMapping("/songs/stream/{token}/{id}")
    private ResponseEntity<Object> streamSong(@PathVariable("id") Long id, @PathVariable("token") String token) {
        try {
            String username = jwtUlti.extractUsername(token);
            Song song = userService.getUserSong(username, id);
            File file = new File(song.getFileUrl());
            String[] nameSplit = file.getName().split("-");
            String filename = "";
            for(int i =1 ; i<nameSplit.length ; i++) {
                filename += nameSplit[i] + "-";
            }
            filename = filename.substring(0, filename.length()-1);
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Content-Disposition", "attachment; filename=\"" +filename+ "\"")
                    .contentType(new MediaType("audio","mp3")) // FLAC - MP3
                    .contentLength(file.length())
                    .body(new InputStreamResource(new FileInputStream(file)));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @GetMapping("/songs/download/{id}")
    private ResponseEntity<Object> downloadUserSong(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Song song = userService.getUserSong(username, id);
            File file = new File(song.getFileUrl());
            String[] nameSplit = file.getName().split("-");
            String filename = "";
            for(int i =1 ; i<nameSplit.length ; i++) {
                filename += nameSplit[i] + "-";
            }
            filename = filename.substring(0, filename.length()-1);
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Content-Disposition", "attachment; filename=\"" +filename+ "\"")
                    .contentType(new MediaType("audio","mp3")) // FLAC - MP3
                    .contentLength(file.length())
                    .body(new InputStreamResource(new FileInputStream(file)));

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @PostMapping("/songs/upload/single")
    private ResponseEntity<Object> uploadUserSong(@RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            userService.addSongToUser(username, file);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("Upload file " + file.getOriginalFilename() + " successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @PostMapping("/songs/upload/multi")
    private ResponseEntity<Object> uploadUserSongs(@RequestParam("files") MultipartFile[] files) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            userService.addSongsToUser(username, files);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("Uploaded " + files.length + " files successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }
}
