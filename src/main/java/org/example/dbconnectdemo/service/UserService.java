package org.example.dbconnectdemo.service;

import org.example.dbconnectdemo.dto.UserDto;
import org.example.dbconnectdemo.model.Song;
import org.example.dbconnectdemo.model.User;

import java.util.List;

public interface UserService{
    User createUser(UserDto userDto);

    User getUserData(String username);

    void deleteUser(String username, String inputPassword);

    List<Song> getUserSong(String username);
}
