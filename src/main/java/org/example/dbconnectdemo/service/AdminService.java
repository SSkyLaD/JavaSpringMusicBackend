package org.example.dbconnectdemo.service;


import org.example.dbconnectdemo.dto.SongDto;
import org.example.dbconnectdemo.dto.UserDto;


import java.util.List;

public interface AdminService {

    List<UserDto> getAllUsersDetailWithSortAndPaging(String adminName, int pageNo, int pageSize, String sortField, String direction);

    List<UserDto> searchAllUsersByNameWithSortAndPaging(String adminName, String username,int pageNo, int pageSize, String sortField, String direction);

    String deleteUser(String adminName, String adminPassword, Long userId);

    List<SongDto> getAllUserSongs(String adminName, Long userId);

    List<SongDto> getAllUserSongsWithSort(String adminName, Long userId, String field, String direction);

    SongDto deleteUserSong(String adminName, Long userId, Long songId);
}
