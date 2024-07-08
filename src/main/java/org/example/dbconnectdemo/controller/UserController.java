package org.example.dbconnectdemo.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.example.dbconnectdemo.dto.*;
import org.example.dbconnectdemo.exception.ResourceNotFoundException;
import org.example.dbconnectdemo.map.SongMapper;
import org.example.dbconnectdemo.model.Song;
import org.example.dbconnectdemo.model.SongList;
import org.example.dbconnectdemo.model.User;
import org.example.dbconnectdemo.service.UserService;
import org.example.dbconnectdemo.spring_security.JwtUlti;
import org.springframework.core.io.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.ArrayList;
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

    private final ResourceLoader resourceLoader;

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
            Data resData = new Data(user.getUsername(), user.getEmail(), user.getCreateDate(), user.getSumOfSongs(), user.getAvailableMemory() / (1024 * 1024));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData("Success", resData));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

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

    //Remove song ra khoi các userlist truoc khi xoa
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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @PatchMapping("/songs/favorites/{id}")
    private ResponseEntity<Object> addUserFavorites(@PathVariable("id") Long id, @RequestBody Map<String, Boolean> isFavorite) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            System.out.println(isFavorite);
            System.out.println(isFavorite.get("isFavorite"));
            SongDto song = userService.updateUserFavoriteSong(username, id, isFavorite.get("isFavorite"));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData("Success!", song));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    //NEW VERSION
    @GetMapping("/songs/stream/{token}/{id}")
    private Mono<ResponseEntity<Resource>> stream(@PathVariable("id") Long id,
                                                  @PathVariable("token") String token,
                                                  @RequestHeader(value = "Range", required = false) String rangeHeader) {
        String username = jwtUlti.extractUsername(token);
        Song song = userService.getUserSong(username, id);
        String filePathString = song.getFileUrl();
        FileSystemResource resource = new FileSystemResource(new File(filePathString));

        long fileSize;
        try {
            fileSize = resource.contentLength();
        } catch (IOException e) {
            return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        }

        if (rangeHeader == null) {
            return Mono.just(ResponseEntity.ok()
                    .contentType(getMediaType(filePathString))
                    .contentLength(fileSize)
                    .body(resource));
        }

        String[] ranges = rangeHeader.replace("bytes=", "").split("-");
        long rangeStart = Long.parseLong(ranges[0]);
        long rangeEnd = ranges.length > 1 ? Long.parseLong(ranges[1]) : fileSize - 1;
        long contentLength = rangeEnd - rangeStart + 1;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize);
        headers.add("Accept-Ranges", "bytes");
        headers.setContentType(getMediaType(filePathString));
        headers.setContentLength(contentLength);

        Mono<Resource> resourceMono = Mono.fromSupplier(() -> {
            try {
                RandomAccessFile file = new RandomAccessFile(filePathString, "r");
                file.seek(rangeStart);
                byte[] data = new byte[(int) contentLength];
                file.readFully(data);
                file.close();
                return new ByteArrayResource(data);
            } catch (IOException e) {
                return null;
            }
        });

        return resourceMono.map(resourceBody -> ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(headers).body(resourceBody));
    }

    private MediaType getMediaType(String filePath) {
        String extension = FilenameUtils.getExtension(filePath);
        switch (extension) {
            case "mp3":
                return MediaType.valueOf("audio/mp3");
            case "flac":
                return MediaType.valueOf("audio/x-flac");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
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
            String subtype = FilenameUtils.getExtension(file.getName()).equals("flac") ? "x-flac" : "mp3";
            for (int i = 1; i < nameSplit.length; i++) {
                filename += nameSplit[i] + "-";
            }
            filename = filename.substring(0, filename.length() - 1);
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .contentType(new MediaType("audio", subtype)) // FLAC - MP3
                    .contentLength(file.length())
                    .body(new InputStreamResource(new FileInputStream(file)));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    // 2024-7-8 Check Storage before upload + If admin Storage dont increase
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

    // 2024-7-8 Check Storage before upload + If admin Storage dont increase
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

    @PostMapping("/lists")
    private ResponseEntity<Object> createUserList(@RequestBody Map<String, String> listName) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            userService.createUserCustomList(username, listName.get("name"));
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseMessage("List " + listName.get("name") + " created successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @GetMapping("/lists")
    private ResponseEntity<Object> getAllUserCustomLists() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            List<SongListDto> songListDto = userService.getAllUserCustomLists(username);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!", songListDto.size(), songListDto));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @GetMapping("/lists/{id}")
    private ResponseEntity<Object> getUserCustomList(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            SongList songList = userService.getUserCustomList(username,id);
            List<SongDto> songDtos = new ArrayList<>();
            for(Song songs : songList.getSongs()){
                songDtos.add(SongMapper.mapToSongDto(songs));
            }
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData("Success!", songDtos));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @DeleteMapping("/lists/{id}")
    private ResponseEntity<Object> deleteUserCustomList(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            SongList songList = userService.deleteUserCustomList(username,id);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("Deleted list " + songList.getName() + " successfully!"));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @PatchMapping("/lists/{id}")
    private ResponseEntity<Object> updateSongList(@PathVariable Long id, @RequestBody Map<String,String> listName) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            SongList songList = userService.updateUserCustomList(username,id,listName.get("name"));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("Song list updated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @PostMapping("/lists/{id}/songs/{songId}")
    private ResponseEntity<Object> addSongToCustomList(@PathVariable Long id, @PathVariable Long songId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            String message = userService.addSongToCustomList(username,id,songId);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.toString()));
        }
    }

    @DeleteMapping("/lists/{id}/songs/{songId}")
    private ResponseEntity<Object> removeSongFromCustomList(@PathVariable Long id, @PathVariable Long songId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            String message = userService.removeSongFromCustomList(username,id,songId);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }
}
