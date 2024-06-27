package org.example.dbconnectdemo.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.dbconnectdemo.dto.UserDto;
import org.example.dbconnectdemo.exception.InvalidInputException;
import org.example.dbconnectdemo.exception.ResourceNotFoundException;
import org.example.dbconnectdemo.exception.UsernameAlreadyExistException;
import org.example.dbconnectdemo.map.UserMapper;
import org.example.dbconnectdemo.model.Song;
import org.example.dbconnectdemo.model.User;
import org.example.dbconnectdemo.repository.UserRepository;
import org.example.dbconnectdemo.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

        User user = UserMapper.mapToUser(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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
        userRepository.deleteUserByUsername(username);
    }

    @Override
    public List<Song> getUserSong(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Cannot find user"));
        return user.getUserStorageList().getSongs();
    }
}
