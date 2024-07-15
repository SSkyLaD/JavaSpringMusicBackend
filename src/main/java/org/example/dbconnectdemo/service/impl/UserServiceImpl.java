package org.example.dbconnectdemo.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.example.dbconnectdemo.dto.SongDto;
import org.example.dbconnectdemo.dto.SongListDto;
import org.example.dbconnectdemo.dto.UserDto;
import org.example.dbconnectdemo.exception.InvalidInputException;
import org.example.dbconnectdemo.exception.ResourceNotFoundException;
import org.example.dbconnectdemo.exception.UsernameAlreadyExistException;
import org.example.dbconnectdemo.map.SongListMapper;
import org.example.dbconnectdemo.map.SongMapper;
import org.example.dbconnectdemo.map.UserMapper;
import org.example.dbconnectdemo.model.Role;
import org.example.dbconnectdemo.model.Song;
import org.example.dbconnectdemo.model.SongList;
import org.example.dbconnectdemo.model.User;
import org.example.dbconnectdemo.repository.SongListRepository;
import org.example.dbconnectdemo.repository.SongRepository;
import org.example.dbconnectdemo.repository.UserRepository;
import org.example.dbconnectdemo.service.Utility;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final SongRepository songRepository;
    private final SongListRepository songlistRepository;

    @Override
    public void createUser(UserDto userDto) {
        if (userDto.getUsername() == null || userDto.getUsername().isEmpty()) {
            throw new InvalidInputException("Username cannot be blank");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isEmpty()) {
            throw new InvalidInputException("Email cannot be blank");
        }
        if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
            throw new InvalidInputException("Password cannot be blank");
        }
        String EMAIL_PATTERN = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!userDto.getEmail().matches(EMAIL_PATTERN)) {
            throw new InvalidInputException("Invalid email address");
        }
        if (userDto.getPassword().length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters");
        }
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistException("Username already exist");
        }

        File userDir = new File(Utility.STATIC_FILE_URL + userDto.getUsername());
        if (!userDir.exists()) {
            if(!userDir.mkdir()){
                throw new RuntimeException("Create directory failed");
            }
        }
        User user = UserMapper.mapToUser(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUserDir(userDir.getAbsolutePath());
        userRepository.save(user);
    }

    @Override
    public User getUserData(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
    }

    @Transactional
    @Override
    public void deleteUser(String username, String inputPassword) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new RuntimeException("Password not match");
        }
        File userDir = new File(user.getUserDir());
        File[] contents = userDir.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        if(!userDir.delete()){
            throw new RuntimeException("Delete directory failed");
        }
        userRepository.deleteUserByUsername(username);
    }

    @Override
    public List<SongDto> getAllUserSongs(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        List<Song> songs = songRepository.findByUserOwnerId(user.getId()); //List<Song> songs = user.getUserSongs();
        List<SongDto> songsdto = new ArrayList<>();
        for (Song song : songs) {
            songsdto.add(SongMapper.mapToSongDto(song));
        }
        return songsdto;
    }


    @Override
    public List<SongDto> getAllUserSongsWithSortAndPaging(String username, int pageNo, int pageSize, String field, String direction) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        Pageable paging = null;
        if(Objects.equals(direction, "asc")){
            paging = PageRequest.of(pageNo, pageSize, Sort.by(field).ascending());
        }
        if(Objects.equals(direction, "desc")){
            paging = PageRequest.of(pageNo, pageSize, Sort.by(field).descending());
        }
        List<Song> songs = songRepository.findAllByUserOwnerId(user.getId(), paging);
        Utility.sortSongs(songs, field, direction);
        List<SongDto> songsDto = new ArrayList<>();
        for (Song aSong : songs) {
            songsDto.add(SongMapper.mapToSongDto(aSong));
        }
        return songsDto;
    }

    @Override
    public List<SongDto> searchAllUserSongsLikeNameWithPaging(String username, int pageNo, int pageSize, String name) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        Pageable paging = PageRequest.of(pageNo, pageSize);
        List<Song> songs = songRepository.findAllByUserOwnerIdAndNameContainingOrArtistContaining(user.getId(),name,name,paging);
        List<SongDto> songsDto = new ArrayList<>();
        for (Song aSong : songs) {
            songsDto.add(SongMapper.mapToSongDto(aSong));
        }
        return songsDto;
    }

    @Override
    public List<SongDto> searchAllUserSongsLikeName(String username, String name) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        List<Song> songs = songRepository.findAllByUserOwnerIdAndNameContainingOrArtistContaining(user.getId(),name,name);
        List<SongDto> songsDto = new ArrayList<>();
        for (Song aSong : songs) {
            songsDto.add(SongMapper.mapToSongDto(aSong));
        }
        return songsDto;
    }


    @Override
    public Song getUserSong(String username, Long songId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        Song song = songRepository.findByIdAndUserOwnerId(songId, user.getId());
        if (song == null) {
            throw new ResourceNotFoundException("Cannot find song");
        }
        return song;
    }

    @Override
    public List<SongDto> getAllUserFavoriteSongs(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        List<Song> favoriteSongs = songRepository.findByUserOwnerIdAndFavorite(user.getId(), true);
        List<SongDto> favoriteSongsDto = new ArrayList<>();
        for (Song song : favoriteSongs) {
            favoriteSongsDto.add(SongMapper.mapToSongDto(song));
        }
        return favoriteSongsDto;
    }

    @Override
    public List<SongDto> getAllUserFavoriteSongsWithSortAndPaging(String username, int pageNo, int pageSize, String field, String direction) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        Pageable paging = null;
        if(Objects.equals(direction, "asc")){
            paging = PageRequest.of(pageNo, pageSize, Sort.by(field).ascending());
        }
        if(Objects.equals(direction, "desc")){
            paging = PageRequest.of(pageNo, pageSize, Sort.by(field).descending());
        }
        List<Song> favoriteSongs = songRepository.findAllByUserOwnerIdAndFavorite(user.getId(),true,paging);
        List<SongDto> songsDto = new ArrayList<>();
        for (Song aSong : favoriteSongs) {
            songsDto.add(SongMapper.mapToSongDto(aSong));
        }
        return songsDto;
    }

    @Override
    public List<SongDto> searchAllUserFavoriteSongsLikeNameWithPaging(String username, int pageNo, int pageSize, String name) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        Pageable paging = PageRequest.of(pageNo, pageSize);
        List<Song> songs = songRepository.findAllByUserOwnerIdAndFavoriteAndNameContainingOrArtistContaining(user.getId(),true,name,name,paging);
        List<SongDto> songsDto = new ArrayList<>();
        for (Song aSong : songs) {
            songsDto.add(SongMapper.mapToSongDto(aSong));
        }
        return songsDto;
    }

    @Override
    public List<SongDto> searchAllUserFavoriteSongsLikeName(String username, String name) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        List<Song> songs = songRepository.findAllByUserOwnerIdAndFavoriteAndNameContainingOrArtistContaining(user.getId(),true,name,name);
        List<SongDto> songsDto = new ArrayList<>();
        for (Song aSong : songs) {
            songsDto.add(SongMapper.mapToSongDto(aSong));
        }
        return songsDto;
    }

    @Override
    public SongDto updateUserFavoriteSong(String username, Long songId, boolean isFavorite) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        for (Song song : user.getUserSongs()) {
            if (songId.equals(song.getId())) {
                song.setFavorite(isFavorite);
                userRepository.save(user);
                return SongMapper.mapToSongDto(song);
            }
        }
        throw new ResourceNotFoundException("Cannot find song");
    }

    @Override
    public void addSongToUser(String username, MultipartFile file) throws IOException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        List<String> allowFileType = new ArrayList<>();
        allowFileType.add("audio/flac");
        allowFileType.add("audio/x-flac");
        allowFileType.add("audio/mpeg");
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if ((!Objects.equals(extension, "flac") && !Objects.equals(extension, "mp3")) || extension.isEmpty()) {
            throw new InvalidInputException("Invalid file type");
        }
        if (!allowFileType.contains(file.getContentType())) {
            throw new InvalidInputException("Invalid file type");
        }
        if (file.getSize() > user.getAvailableMemory()) {
            throw new InvalidInputException("Memory limit exceeded");
        }
        Song song = new Song();
        song.setFileName(file.getOriginalFilename());
        String fileUrl = user.getUserDir() + "\\" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
        song.setFileUrl(fileUrl);
        file.transferTo(new File(fileUrl));
        song.setSize(file.getSize());

        AudioFile audioFile = AudioFileIO.read(new File(fileUrl));
        AudioHeader audioHeader = audioFile.getAudioHeader();
        Tag tag = audioFile.getTag();

        String[] filenameSplit = Objects.requireNonNull(file.getOriginalFilename()).split("-");
        String title = filenameSplit[filenameSplit.length - 1];
        StringBuilder artist = new StringBuilder();
        title = title.replaceAll("\\.(flac|mp3)$", "").trim();

        for (int i = 0; i < filenameSplit.length - 1; i++) {
            artist.append(filenameSplit[i]).append(" ");
        }
        artist = new StringBuilder(artist.toString().trim());

        song.setName(title);
        song.setArtist(artist.toString());

        if (tag != null) {
            if (!tag.getFirst(FieldKey.TITLE).isEmpty()) {
                song.setName(tag.getFirst(FieldKey.TITLE));
            }
            if (!tag.getFirst(FieldKey.ARTIST).isEmpty()) {
                song.setArtist(tag.getFirst(FieldKey.ARTIST));
            }
            song.setAlbum(tag.getFirst(FieldKey.ALBUM));
            song.setReleaseDate(tag.getFirst(FieldKey.YEAR));
            Artwork artwork = tag.getFirstArtwork();
            if (artwork != null) {
                byte[] imageBytes = artwork.getBinaryData();
                song.setAlbumImageBase64("data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes));
            }
        }
        song.setDuration(audioHeader.getTrackLength());



        user.getUserSongs().add(song);
        user.setSumOfSongs(user.getUserSongs().size());
        if (!user.getRole().equals(Role.ADMIN)) {
            user.setAvailableMemory(user.getAvailableMemory() - song.getSize());
        }
        userRepository.save(user);
    }

    @Override
    public List<String> addSongsToUser(String username, MultipartFile[] files) throws IOException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        List<String> allowFileType = new ArrayList<>();
        allowFileType.add("audio/flac");
        allowFileType.add("audio/x-flac");
        allowFileType.add("audio/mpeg");
        List<String> result = new ArrayList<>();
        for (MultipartFile file : files) {
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            if ((!Objects.equals(extension, "flac") && !Objects.equals(extension, "mp3")) || extension.isEmpty()) {
                result.add("File " + file.getOriginalFilename() + " is invalid");
                continue;
            }
            if (!allowFileType.contains(file.getContentType())) {
                result.add("File " + file.getOriginalFilename() + " is invalid");
                continue;
            }
            if (file.getSize() > user.getAvailableMemory()) {
                throw new InvalidInputException("Memory limit exceeded");
            }
            Song song = new Song();
            song.setFileName(file.getOriginalFilename());
            String fileUrl = user.getUserDir() + "\\" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
            song.setFileUrl(fileUrl);
            file.transferTo(new File(fileUrl));
            song.setSize(file.getSize());

            AudioFile audioFile = AudioFileIO.read(new File(fileUrl));
            AudioHeader audioHeader = audioFile.getAudioHeader();
            Tag tag = audioFile.getTag();

            String[] filenameSplit = Objects.requireNonNull(file.getOriginalFilename()).split("-");
            String title = filenameSplit[filenameSplit.length - 1];
            StringBuilder artist = new StringBuilder();
            title = title.replaceAll("\\.(flac|mp3)$", "").trim();

            for (int i = 0; i < filenameSplit.length - 1; i++) {
                artist.append(filenameSplit[i]).append(" ");
            }
            artist = new StringBuilder(artist.toString().trim());

            song.setName(title);
            song.setArtist(artist.toString());

            if (tag != null) {
                if (!tag.getFirst(FieldKey.TITLE).isEmpty()) {
                    song.setName(tag.getFirst(FieldKey.TITLE));
                }
                if (!tag.getFirst(FieldKey.ARTIST).isEmpty()) {
                    song.setArtist(tag.getFirst(FieldKey.ARTIST));
                }
                song.setAlbum(tag.getFirst(FieldKey.ALBUM));
                song.setReleaseDate(tag.getFirst(FieldKey.YEAR));
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageBytes = artwork.getBinaryData();
                    song.setAlbumImageBase64("data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes));
                }
            }
            song.setDuration(audioHeader.getTrackLength());

            System.out.print(song.getArtist() + " " + song.getName() + " " + song.getFileUrl());


            user.getUserSongs().add(song);
            user.setSumOfSongs(user.getSumOfSongs() + 1);
            if (!user.getRole().equals(Role.ADMIN)) {
                user.setAvailableMemory(user.getAvailableMemory() - song.getSize());
            }
            userRepository.save(user);
        }
        return result;
    }

    @Override
    public SongDto deleteSongFromUser(String username, Long id) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        List<Song> songs = user.getUserSongs();
        for (Song song : songs) {
            if (song.getId().equals(id)) {
                for (SongList list : user.getUserSongLists()) {
                    if (list.getSongs().contains(song)) {
                        list.setSumOfSongs(list.getSumOfSongs() - 1);
                        list.getSongs().remove(song);
                        break;
                    }
                }
                File songFile = new File(song.getFileUrl());
                user.setSumOfSongs(user.getSumOfSongs() - 1);
                user.setAvailableMemory(user.getAvailableMemory() + song.getSize());
                if(!songFile.delete()){
                    throw new RuntimeException("File delete failed");
                }
                songs.remove(song);
                songRepository.deleteById(id);
                user.setUserSongs(songs);
                userRepository.save(user);
                return SongMapper.mapToSongDto(song);
            }
        }
        throw new ResourceNotFoundException("Cannot find song");
    }

    @Override
    public void createUserCustomList(String username, String listName) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        SongList newList = new SongList(listName.trim());
        for (SongList list : user.getUserSongLists()) {
            if (list.getName().equals(listName)) {
                throw new InvalidInputException("List " + listName + " already exists");
            }
        }
        user.getUserSongLists().add(newList);
        userRepository.save(user);
    }

    @Override
    public List<SongListDto> getAllUserCustomLists(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        List<SongListDto> songListDto = new ArrayList<>();
        for (SongList songList : user.getUserSongLists()) {
            songListDto.add(SongListMapper.mapToCustomListDto(songList));
        }
        return songListDto;
    }

    @Override
    public SongList getUserCustomList(String username, Long id) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        for (SongList songList : user.getUserSongLists()) {
            if (songList.getId().equals(id)) {
                return songList;
            }
        }
        throw new ResourceNotFoundException("List not found");
    }

    @Override
    public SongList deleteUserCustomList(String username, Long id) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        for (SongList songList : user.getUserSongLists()) {
            if (songList.getId().equals(id)) {
                user.getUserSongLists().remove(songList);
                songlistRepository.deleteById(id);
                userRepository.save(user);
                return songList;
            }
        }
        throw new ResourceNotFoundException("List not found");
    }

    @Override
    public SongList updateUserCustomList(String username, Long id, String listName) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        for (SongList list : user.getUserSongLists()) {
            if (list.getId().equals(id)) {
                list.setName(listName);
                userRepository.save(user);
                return list;
            }
        }
        throw new ResourceNotFoundException("List not found");
    }

    @Override
    public String addSongToCustomList(String username, Long listId, Long songId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        for (SongList list : user.getUserSongLists()) {
            if (list.getId().equals(listId)) {
                if (list.getSumOfSongs() == 30) {
                    throw new InvalidInputException("List is full");
                }
                for (Song song : user.getUserSongs()) {
                    if (song.getId().equals(songId)) {
                        list.getSongs().add(song);
                        list.setSumOfSongs(list.getSongs().size());
                        userRepository.save(user);
                        return "Song " + song.getArtist() + " - " + song.getName() + " added to " + list.getName() + " successfully";
                    }
                }
            }
        }
        throw new ResourceNotFoundException("List or song not found");
    }

    @Override
    public String removeSongFromCustomList(String username, Long listId, Long songId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        for (SongList list : user.getUserSongLists()) {
            if (list.getId().equals(listId)) {
                for (Song song : user.getUserSongs()) {
                    if (song.getId().equals(songId)) {
                        list.getSongs().remove(song);
                        list.setSumOfSongs(list.getSongs().size());
                        userRepository.save(user);
                        return "Song " + song.getArtist() + " - " + song.getName() + " removed from " + list.getName() + " successfully";
                    }
                }
            }
        }
        throw new ResourceNotFoundException("List or song not found");
    }
}
