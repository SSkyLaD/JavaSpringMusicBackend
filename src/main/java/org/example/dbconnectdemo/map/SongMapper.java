package org.example.dbconnectdemo.map;

import org.example.dbconnectdemo.dto.SongDto;
import org.example.dbconnectdemo.model.Song;

public class SongMapper {
    public static SongDto mapToSongDto(Song song) {
        SongDto songDto = new SongDto();
        songDto.setId(song.getId());
        songDto.setUploadDate(song.getUploadDate());
        songDto.setFileName(song.getFileName());
        songDto.setName(song.getName());
        songDto.setArtist(song.getArtist());
        songDto.setAlbum(song.getAlbum());
        songDto.setAlbumImageBase64(song.getAlbumImageBase64());
        songDto.setReleaseDate(song.getReleaseDate());
        songDto.setDuration(song.getDuration());
        songDto.setSize((double) Math.round((song.getSize() / (1024 * 1024) * 100)) / 100);
        songDto.setFavorite(song.isFavorite());
        return songDto;
    }
}
