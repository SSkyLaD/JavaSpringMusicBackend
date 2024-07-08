package org.example.dbconnectdemo.service;


import org.example.dbconnectdemo.dto.SongDto;
import org.example.dbconnectdemo.dto.SongListDto;
import org.example.dbconnectdemo.dto.UserDto;


import java.util.List;

public interface AdminService {
    List<UserDto> getAllUsersDetail(String adminName);

    String deleteUser(String adminName, String adminPassword, Long userId);

    List<SongDto> getUserSongs(String adminName, Long userId);

    SongDto deleteUserSong(String adminName, Long userId, Long songId);
}
