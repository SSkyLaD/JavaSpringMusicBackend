package org.example.dbconnectdemo.service;

import org.example.dbconnectdemo.model.Song;
import org.springframework.beans.factory.annotation.Value;

import java.security.MessageDigest;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Utility {

    @Value("${FRONTEND_URL}")
    public static String FRONTEND_URl;
    
    @Value("${STATIC_FILE_URL}")
    public static String STATIC_FILE_URL;

    //Config lai application.property
    @Value("${FRONTEND_ID_KEY}")
    public static String FRONTEND_ID_KEY = "4PvBlqCTz5";

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

    public static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
