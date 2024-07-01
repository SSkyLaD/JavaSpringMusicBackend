package org.example.dbconnectdemo.service.impl;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.dbconnectdemo.dto.UserDto;
import org.example.dbconnectdemo.exception.InvalidInputException;
import org.example.dbconnectdemo.exception.ResourceNotFoundException;
import org.example.dbconnectdemo.exception.UsernameAlreadyExistException;
import org.example.dbconnectdemo.map.UserMapper;
import org.example.dbconnectdemo.model.Song;
import org.example.dbconnectdemo.model.StorageList;
import org.example.dbconnectdemo.model.User;
import org.example.dbconnectdemo.repository.UserRepository;
import org.example.dbconnectdemo.service.UserService;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static org.apache.catalina.startup.ExpandWar.deleteDir;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.example.dbconnectdemo.repository.songRepository songRepository;

    @Override
    public User createUser(UserDto userDto) {
        if(userDto.getUsername() == null || userDto.getUsername().isEmpty()){
            throw new InvalidInputException("Username cannot be blank");
        }
        if(userDto.getEmail() == null || userDto.getEmail().isEmpty()){
            throw new InvalidInputException("Email cannot be blank");
        }
        if(userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
            throw new InvalidInputException("Password cannot be blank");
        }
        String EMAIL_PATTERN = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if(!userDto.getEmail().matches(EMAIL_PATTERN)) {
            throw new InvalidInputException("Invalid email address");
        }
        if(userDto.getPassword().length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters");
        }
        if(userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistException("Username already exist");
        }

        //Todo add pathname to application.properties
        File userDir = new File("D:\\LaD\\Study\\InternSummer2024\\JavaSpringMusicBackend\\src\\main\\resources\\" + userDto.getUsername());
        if(!userDir.exists()) {
            userDir.mkdir();
        }
        User user = UserMapper.mapToUser(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.getUserStorageList().setLocalStorageUrl(userDir.getAbsolutePath());
        return userRepository.save(user);
    }

    @Override
    public User getUserData(String username) {
        return userRepository.findByUsername(username).orElseThrow(()->new ResourceNotFoundException("Cannot find user"));
    }

    @Transactional
    @Override
    public void deleteUser(String username, String inputPassword) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        System.out.println(username);
        System.out.println(inputPassword);
        System.out.println(user.getPassword());
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new RuntimeException("Password not match");
        }
        File userDir = new File(user.getUserStorageList().getLocalStorageUrl());
        File[] contents = userDir.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        userDir.delete();
        userRepository.deleteUserByUsername(username);
    }

    //Todo Paging response data
    @Override
    public List<Song> getUserSongs(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        return user.getUserStorageList().getSongs();
    }

    @Override
    public void addSongToUser(String username, MultipartFile file) throws IOException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException {
        List<String> allowFileType = new ArrayList<>();
        allowFileType.add("audio/flac");
        allowFileType.add("audio/x-flac");
        allowFileType.add("audio/mp3");
        if (!allowFileType.contains(file.getContentType())) {
            throw new InvalidInputException("Invalid file type");
        }
        Song song = new Song();
        song.setFileName(file.getOriginalFilename());
        String fileUrl = "D:\\LaD\\Study\\InternSummer2024\\JavaSpringMusicBackend\\src\\main\\resources\\" + username + "\\" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
        song.setFileUrl(fileUrl);
        file.transferTo(new File(fileUrl));
        song.setSize(file.getSize());

        AudioFile audioFile = AudioFileIO.read(new File(fileUrl));
        AudioHeader audioHeader = audioFile.getAudioHeader();
        Tag tag = audioFile.getTag();

        String[] filenameSplit = Objects.requireNonNull(file.getOriginalFilename()).split("-");
        String artist = filenameSplit[filenameSplit.length -1];
        String title = "";
        artist = artist.replaceAll("\\.(flac|mp3)$", "").trim();

        for(int i = 0; i < filenameSplit.length - 1; i++) {
            title += filenameSplit[i] + " ";
        }
        title = title.trim();

        song.setName(title);
        song.setArtist(artist);

        if (tag != null) {
            song.setName(tag.getFirst(FieldKey.TITLE));
            song.setArtist(tag.getFirst(FieldKey.ARTIST));
            song.setAlbum(tag.getFirst(FieldKey.ALBUM));
            song.setReleaseDate(tag.getFirst(FieldKey.YEAR));
            Artwork artwork = tag.getFirstArtwork();
            if (artwork != null) {
                byte[] imageBytes = artwork.getBinaryData();
                song.setAlbumImageBase64(Base64.getEncoder().encodeToString(imageBytes));
            }
        }
        song.setDuration(audioHeader.getTrackLength());
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        user.getUserStorageList().getSongs().add(song);
        user.getUserStorageList().setSumOfSongs(user.getUserStorageList().getSumOfSongs() + 1);
        user.getUserStorageList().setAvailableMemory(user.getUserStorageList().getAvailableMemory() - song.getSize());
        userRepository.save(user);
    }

    @Override
    public void deleteSongFromUser(String username, Long id) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        List<Song> songs = user.getUserStorageList().getSongs();
        for (Song song : songs) {
            if (song.getId().equals(id)) {
                File songFile = new File(song.getFileUrl());
                user.getUserStorageList().setSumOfSongs(user.getUserStorageList().getSumOfSongs() - 1);
                user.getUserStorageList().setAvailableMemory(user.getUserStorageList().getAvailableMemory() + song.getSize());
                songFile.delete();
                songs.remove(song);
                songRepository.deleteById(id);
                user.getUserStorageList().setSongs(songs);
                userRepository.save(user);
                return;
            }
        }
        throw new ResourceNotFoundException("Cannot find song");
    }
}
