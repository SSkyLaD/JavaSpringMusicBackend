package org.example.dbconnectdemo.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.example.dbconnectdemo.dto.*;
import org.example.dbconnectdemo.exception.InvalidInputException;
import org.example.dbconnectdemo.exception.ResourceNotFoundException;
import org.example.dbconnectdemo.map.SongMapper;
import org.example.dbconnectdemo.model.Song;
import org.example.dbconnectdemo.model.SongList;
import org.example.dbconnectdemo.model.User;
import org.example.dbconnectdemo.service.UserService;
import org.example.dbconnectdemo.spring_security.JwtUlti;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
//@CrossOrigin(origins = "http://localhost:5150")
@RequestMapping("/api/v1/users")
public class UserController {

    private final JwtUlti jwtUlti;
    private final UserService userService;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> maxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage() + " (200MB)"));
    }

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


    // Allow Sort by all field in song, uploadDate ASC and DESC
    // api/v1/users/songs?pageNo=0&sortField=name&direction=asc
    @GetMapping("/songs")
    private ResponseEntity<Object> getUserSongs(@RequestParam(required = false) Map<String, String> qparams) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            if (!qparams.isEmpty()) {
                int pageNo = qparams.get("pageNo") == null ? 0 : Integer.parseInt(qparams.get("pageNo"));
                int pageSize = 20; // fix pageSize = 20;
                String field = qparams.get("sortField") == null ? "uploadDate" : qparams.get("sortField");
                String direction = qparams.get("direction") == null ? "asc" : qparams.get("direction");
                List<SongDto> data = userService.getAllUserSongsWithSortAndPaging(username,pageNo,pageSize, field, direction);
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!", data.size(), data));
            }

            //For old frontend
            List<SongDto> data = userService.getAllUserSongs(username);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!", data.size(), data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }


    // /api/v1/users/songs/search?(name=name||artist=artist)&pageNo=0&sortField=name&direction=asc
    @GetMapping("/songs/search")
    private ResponseEntity<Object> searchUserSongs(@RequestParam Map<String,String> params){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            if(params.get("pageNo") != null){
                String name = params.get("name") == null ? "" : params.get("name");
                String artist = params.get("artist") == null ? "" : params.get("artist");
                int pageNo = Integer.parseInt(params.get("pageNo"));
                int pageSize = 20; // fix pageSize = 20;
                String sortField = params.get("sortField") == null ? "uploadDate" : params.get("sortField");
                String direction = params.get("direction") == null ? "asc" : params.get("direction");
                if(!name.isEmpty()){
                    List<SongDto> data = userService.searchAllUserSongsLikeNameWithSortAndPaging(username,pageNo,pageSize,sortField,direction,name);
                    return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!", data.size(), data));
                }
                if(!artist.isEmpty()){
                    List<SongDto> data = userService.searchAllUserSongsLikeArtistWithSortAndPaging(username,pageNo,pageSize,sortField,direction,artist);
                    return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!", data.size(), data));
                }
            }
            String name = params.get("name") == null ? "" : params.get("name");
            List<SongDto> data = userService.searchAllUserSongsLikeName(username,name);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!", data.size(), data));
        } catch (Exception e){
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

    // Allow Sort by Name, Artist, Duration, Size, uploadDate ASC and DESC
    // /api/v1/users/songs/favorites?pageNo=0&sortField=name&direction=desc
    @GetMapping("/songs/favorites")
    private ResponseEntity<Object> getUserFavorites(@RequestParam(required = false) Map<String, String> qparams) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            if (!qparams.isEmpty()) {
                int pageNo = qparams.get("pageNo") == null ? 0 : Integer.parseInt(qparams.get("pageNo"));
                int pageSize = 20; // fix pageSize = 20;
                String field = qparams.get("sortField") == null ? "uploadDate" : qparams.get("sortField");
                String direction = qparams.get("direction") == null ? "asc" : qparams.get("direction");
                List<SongDto> data = userService.getAllUserFavoriteSongsWithSortAndPaging(username,pageNo,pageSize, field, direction);
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!", data.size(), data));
            }
            List<SongDto> data = userService.getAllUserFavoriteSongs(username);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!", data.size(), data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    // /api/v1/users/songs/favorite/search?name=name&pageNo=0&sortField=name&direction=asc
    @GetMapping("/songs/favorites/search")
    private ResponseEntity<Object> searchUserFavoriteSongs(@RequestParam Map<String,String> params){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            if(params.get("pageNo") != null){
                String name = params.get("name") == null ? "" : params.get("name");
                String artist = params.get("artist") == null ? "" : params.get("artist");
                int pageNo = Integer.parseInt(params.get("pageNo"));
                int pageSize = 20; // fix pageSize = 20;
                String sortField = params.get("sortField") == null ? "uploadDate" : params.get("sortField");
                String direction = params.get("direction") == null ? "asc" : params.get("direction");
                if(!name.isEmpty()){
                    List<SongDto> data = userService.searchAllUserFavoriteSongsLikeNameWithSortAndPaging(username,pageNo,pageSize,sortField,direction,name);
                    return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!", data.size(), data));
                }
                if(!artist.isEmpty()){
                    List<SongDto> data = userService.searchAllUserFavoriteSongsLikeArtistWithSortAndPaging(username,pageNo,pageSize,sortField,direction,artist);
                    return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!", data.size(), data));
                }
            }
            String name = params.get("name") == null ? "" : params.get("name");
            List<SongDto> data = userService.searchAllUserFavoriteSongsLikeName(username,name);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseDataList("Success!", data.size(), data));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @PatchMapping("/songs/favorites/{id}")
    private ResponseEntity<Object> addUserFavorites(@PathVariable("id") Long id, @RequestBody Map<String, Boolean> isFavorite) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            SongDto song = userService.updateUserFavoriteSong(username, id, isFavorite.get("isFavorite"));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData("Success!", song));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @GetMapping("/songs/stream/{token}/{id}")
    private Mono<ResponseEntity<Resource>> stream(@PathVariable("id") Long id, @PathVariable("token") String token, @RequestHeader(value = "Range", required = false) String rangeHeader) {
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
            return Mono.just(ResponseEntity.ok().contentType(getMediaType(filePathString)).contentLength(fileSize).body(resource));
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
        return switch (extension) {
            case "mp3" -> MediaType.valueOf("audio/mp3");
            case "flac" -> MediaType.valueOf("audio/x-flac");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    @GetMapping("/songs/download/{id}")
    private ResponseEntity<Object> downloadUserSong(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Song song = userService.getUserSong(username, id);
            File file = new File(song.getFileUrl());
            String subtype = FilenameUtils.getExtension(file.getName()).equals("flac") ? "x-flac" : "mp3";
            return ResponseEntity.status(HttpStatus.OK).header("Content-Disposition", "attachment; filename=\"" + song.getFileName() + "\"").contentType(new MediaType("audio", subtype)) // FLAC - MP3
                    .contentLength(file.length()).body(new InputStreamResource(new FileInputStream(file)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @PostMapping("/songs/upload/single")
    private ResponseEntity<Object> uploadUserSong(@RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            userService.addSongToUser(username, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseMessage("Upload file " + file.getOriginalFilename() + " successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @PostMapping("/songs/upload/multi")
    private ResponseEntity<Object> uploadUserSongs(@RequestParam("files") MultipartFile[] files) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            List<String> result = userService.addSongsToUser(username, files);
            int uploaded = files.length - result.size();
            if (uploaded == 0) {
                throw new InvalidInputException("No files uploaded");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseMessage("Uploaded " + uploaded + " files successful"));
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
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseMessage("Playlist " + listName.get("name") + " created successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @GetMapping("/lists/{id}")
    private ResponseEntity<Object> getUserCustomList(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            SongList songList = userService.getUserCustomList(username, id);
            List<SongDto> songDtos = new ArrayList<>();
            for (Song songs : songList.getSongs()) {
                songDtos.add(SongMapper.mapToSongDto(songs));
            }
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData("Success!", songDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @DeleteMapping("/lists/{id}")
    private ResponseEntity<Object> deleteUserCustomList(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            SongList songList = userService.deleteUserCustomList(username, id);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("Deleted playlist " + songList.getName() + " successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @PatchMapping("/lists/{id}")
    private ResponseEntity<Object> updateSongList(@PathVariable Long id, @RequestBody Map<String, String> listName) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            SongList songList = userService.updateUserCustomList(username, id, listName.get("name"));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("Playlist name updated to " + songList.getName() + " successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @PostMapping("/lists/{id}/songs/{songId}")
    private ResponseEntity<Object> addSongToCustomList(@PathVariable Long id, @PathVariable Long songId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            String message = userService.addSongToCustomList(username, id, songId);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }

    @DeleteMapping("/lists/{id}/songs/{songId}")
    private ResponseEntity<Object> removeSongFromCustomList(@PathVariable Long id, @PathVariable Long songId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            String message = userService.removeSongFromCustomList(username, id, songId);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(e.getMessage()));
        }
    }
}
