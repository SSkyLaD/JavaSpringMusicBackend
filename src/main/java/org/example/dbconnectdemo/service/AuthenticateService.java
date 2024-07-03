package org.example.dbconnectdemo.service;

import org.example.dbconnectdemo.dto.UserDto;

public interface AuthenticateService {
    void register(UserDto userDto);
    String login(UserDto userDto);
}
