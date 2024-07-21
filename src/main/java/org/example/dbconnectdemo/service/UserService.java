package org.example.dbconnectdemo.service;

import org.example.dbconnectdemo.dto.SongListDto;
import org.example.dbconnectdemo.dto.SongDto;
import org.example.dbconnectdemo.dto.UserDto;
import org.example.dbconnectdemo.model.Song;
import org.example.dbconnectdemo.model.SongList;
import org.example.dbconnectdemo.model.User;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface UserService{
    void createUser(UserDto userDto);

    User getUserData(String username);

    void deleteUser(String username, String inputPassword);

    List<SongDto> getAllUserSongs(String username);

    List<SongDto> getAllUserSongsWithSortAndPaging(String username,int pageNo,int pageSize, String field, String direction);

    List<SongDto> searchAllUserSongsLikeNameWithSortAndPaging(String username,int pageNo,int pageSize,String sortField, String direction, String name);

    List<SongDto> searchAllUserSongsLikeArtistWithSortAndPaging(String username,int pageNo,int pageSize,String sortField, String direction, String name);

    //legacy method
    List<SongDto> searchAllUserSongsLikeName(String username, String name);

    Song getUserSong(String username, Long songId);

    List<SongDto> getAllUserFavoriteSongs(String username);

    List<SongDto> getAllUserFavoriteSongsWithSortAndPaging(String username,int pageNo,int pageSize, String field, String direction);

    List<SongDto> searchAllUserFavoriteSongsLikeNameWithSortAndPaging(String username, int pageNo, int pageSize,String sortField, String direction, String name);

    List<SongDto> searchAllUserFavoriteSongsLikeArtistWithSortAndPaging(String username, int pageNo, int pageSize,String sortField, String direction, String name);

    //legacy method
    List<SongDto> searchAllUserFavoriteSongsLikeName(String username, String name);

    SongDto updateUserFavoriteSong(String username, Long songId, boolean isFavorite);

    void addSongToUser(String username, MultipartFile file) throws IOException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException;

    List<String> addSongsToUser(String username, MultipartFile[] files) throws IOException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException;

    SongDto deleteSongFromUser(String username, Long id);

    void createUserCustomList(String username, String listName);

    List<SongListDto> getAllUserCustomLists(String username);

    SongList getUserCustomList(String username, Long id);

    SongList deleteUserCustomList(String username, Long id);

    SongList updateUserCustomList(String username, Long id, String listName);

    String addSongToCustomList(String username, Long listId, Long songId);

    String removeSongFromCustomList(String username, Long listId, Long songId);
}
