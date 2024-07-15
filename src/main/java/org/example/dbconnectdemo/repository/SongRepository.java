package org.example.dbconnectdemo.repository;

import org.example.dbconnectdemo.model.Song;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;


public interface SongRepository extends PagingAndSortingRepository<Song, Long> {

    List<Song> findByUserOwnerId(Long userOwnerId);

    List<Song> findAllByUserOwnerId(Long userOwnerId, Pageable pageable);

    List<Song> findAllByUserOwnerIdAndNameContainingOrArtistContaining(Long userOwnerId, String name,String artist);

    List<Song> findAllByUserOwnerIdAndNameContainingOrArtistContaining(Long userOwnerId, String name,String artist, Pageable pageable);

    List<Song> findAllByUserOwnerIdAndFavoriteAndNameContainingOrArtistContaining(Long userOwnerId, boolean favorite, String name,String artist);

    List<Song> findAllByUserOwnerIdAndFavoriteAndNameContainingOrArtistContaining(Long userOwnerId, boolean favorite, String name,String artist, Pageable pageable);

    List<Song> findAllByUserOwnerIdAndFavorite(Long userOwnerId, boolean favorite,Pageable pageable);

    Song findByIdAndUserOwnerId(Long songId, Long userID);

    List<Song> findByUserOwnerIdAndFavorite(Long songId, boolean favorite);

    void deleteById(Long id);
}
