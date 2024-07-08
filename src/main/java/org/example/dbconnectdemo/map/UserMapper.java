package org.example.dbconnectdemo.map;

import org.example.dbconnectdemo.dto.UserDto;
import org.example.dbconnectdemo.model.User;

public class UserMapper {
    public static UserDto mapToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setPassword("***");
        userDto.setRole(user.getRole());
        userDto.setCreateDate(user.getCreateDate());
        userDto.setAvailableMemory(user.getAvailableMemory());
        userDto.setSumOfSongs(user.getSumOfSongs());
        return userDto;
    }

    public static User mapToUser(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        user.setRole(userDto.getRole());
        user.setEmail(userDto.getEmail());
        return user;
    }
}
