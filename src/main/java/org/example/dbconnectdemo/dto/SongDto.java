package org.example.dbconnectdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SongDto {
    private Long id;

    private Date uploadDate;

    private String name;

    private String artist;

    private String albumImageBase64;

    private String releaseDate;

    private int duration;

    private boolean favorite = false;
}