package org.example.dbconnectdemo.service;

import org.example.dbconnectdemo.dto.RegisterBody;
import org.example.dbconnectdemo.dto.UserDto;

public interface AuthenticateService {
    void register(RegisterBody registerBody);
    String login(UserDto userDto);
}
