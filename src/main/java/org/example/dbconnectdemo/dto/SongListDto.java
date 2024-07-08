package org.example.dbconnectdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SongListDto {
    private Long id;
    private String name;
    private Date createDate;
    private int sumOfSongs;
}
