package org.example.dbconnectdemo.service;

import org.example.dbconnectdemo.dto.UserDto;
import org.example.dbconnectdemo.model.Song;
import org.example.dbconnectdemo.model.User;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService{
    User createUser(UserDto userDto);

    User getUserData(String username);

    void deleteUser(String username, String inputPassword);

    List<Song> getUserSongs(String username);

    void addSongToUser(String username, MultipartFile file) throws IOException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException;

    void deleteSongFromUser(String username, Long id);
}
