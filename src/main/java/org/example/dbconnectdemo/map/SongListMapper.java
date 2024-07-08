package org.example.dbconnectdemo.map;

import org.example.dbconnectdemo.dto.SongListDto;
import org.example.dbconnectdemo.model.SongList;

public class SongListMapper {
    public static SongListDto mapToCustomListDto(SongList songList){
        return new SongListDto(songList.getId(),songList.getName(),songList.getCreateDate(),songList.getSumOfSongs());
    }
}
