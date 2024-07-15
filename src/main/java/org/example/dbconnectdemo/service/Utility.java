package org.example.dbconnectdemo.service;

import org.example.dbconnectdemo.model.Song;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Utility {

    @Value("${FRONTEND_URL}")
    public static String FRONTEND_URl;
    
    @Value("${STATIC_FILE_URL}")
    public static String STATIC_FILE_URL;
    
    public static void sortSongs(List<Song> songs, String field, String direction){
        if(field.equals("name")){
            songs.sort(Comparator.comparing(Song::getName));
        }
        if(field.equals("artist")){
            songs.sort(Comparator.comparing(Song::getArtist));
        }
        if(field.equals("duration")){
            songs.sort(Comparator.comparing(Song::getDuration));
        }
        if(field.equals("size")){
            songs.sort(Comparator.comparing(Song::getSize));
        }
        if(field.equals("uploadDate")){
            songs.sort(Comparator.comparing(Song::getUploadDate));
        }
        if(direction.equals("desc")){
            Collections.reverse(songs);
        }
    }
}
