package org.example.dbconnectdemo.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.dbconnectdemo.dto.SongDto;
import org.example.dbconnectdemo.dto.UserDto;
import org.example.dbconnectdemo.exception.NotAuthorizeException;
import org.example.dbconnectdemo.exception.ResourceNotFoundException;
import org.example.dbconnectdemo.map.SongMapper;
import org.example.dbconnectdemo.map.UserMapper;
import org.example.dbconnectdemo.model.Role;
import org.example.dbconnectdemo.model.Song;
import org.example.dbconnectdemo.model.SongList;
import org.example.dbconnectdemo.model.User;
import org.example.dbconnectdemo.repository.SongRepository;
import org.example.dbconnectdemo.repository.UserRepository;
import org.example.dbconnectdemo.service.AdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.apache.catalina.startup.ExpandWar.deleteDir;

@Service
@AllArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SongRepository songRepository;

    @Override
    public List<UserDto> getAllUsersDetail(String adminName) {
        User admin = userRepository.findByUsername(adminName).orElseThrow(()-> new ResourceNotFoundException("Cannot find user"));
        if (!admin.getRole().equals(Role.ADMIN)){
            throw new NotAuthorizeException();
        }
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = new ArrayList<>();
        for(User appUser : users){
            if (appUser.getRole().equals(Role.ADMIN)){
                continue;
            }
            userDtos.add(UserMapper.mapToUserDto(appUser));
        }
        return userDtos;
    }

    @Transactional
    @Override
    public String deleteUser(String adminName,String adminPassword, Long userId) {
        User admin = userRepository.findByUsername(adminName).orElseThrow(()-> new ResourceNotFoundException("Cannot find user"));
        if (!admin.getRole().equals(Role.ADMIN)){
            throw new NotAuthorizeException();
        }
        if(!passwordEncoder.matches(adminPassword,admin.getPassword())){
            throw new RuntimeException("Password not match");
        }
        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("Cannot find user"));
        File userDir = new File(user.getUserDir());
        File[] contents = userDir.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        userDir.delete();
        userRepository.deleteUserByUsername(user.getUsername());
        return user.getUsername();
    }

    @Override
    public List<SongDto> getUserSongs(String adminName, Long userId) {
        User admin = userRepository.findByUsername(adminName).orElseThrow(()-> new ResourceNotFoundException("Cannot find user"));
        if (!admin.getRole().equals(Role.ADMIN)){
            throw new NotAuthorizeException();
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        List<Song> songs = user.getUserSongs();
        List<SongDto> songsdto = new ArrayList<>();
        for (Song song : songs) {
            songsdto.add(SongMapper.mapToSongDto(song));
        }
        return songsdto;
    }

    @Override
    public SongDto deleteUserSong(String adminName, Long userId, Long songId) {
        User admin = userRepository.findByUsername(adminName).orElseThrow(()-> new ResourceNotFoundException("Cannot find user"));
        if (!admin.getRole().equals(Role.ADMIN)){
            throw new NotAuthorizeException();
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        List<Song> songs = user.getUserSongs();
        for (Song song : songs) {
            if (song.getId().equals(songId)) {
                for (SongList list : user.getUserSongLists()) {
                    if (list.getSongs().contains(song)) {
                        list.setSumOfSongs(list.getSumOfSongs() - 1);
                        list.getSongs().remove(song);
                    }
                }
                File songFile = new File(song.getFileUrl());
                user.setSumOfSongs(user.getSumOfSongs() - 1);
                user.setAvailableMemory(user.getAvailableMemory() + song.getSize());
                songFile.delete();
                songs.remove(song);
                songRepository.deleteById(songId);
                user.setUserSongs(songs);
                userRepository.save(user);
                return SongMapper.mapToSongDto(song);
            }
        }
        throw new ResourceNotFoundException("Cannot find song");
    }
}
