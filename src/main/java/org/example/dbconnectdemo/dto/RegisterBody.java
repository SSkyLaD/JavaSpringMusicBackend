package org.example.dbconnectdemo.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterBody {
    private String username;
    private String email;
    private String password;
    private String checkSum;
}
