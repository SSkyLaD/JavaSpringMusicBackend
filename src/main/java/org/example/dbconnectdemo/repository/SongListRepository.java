package org.example.dbconnectdemo.repository;

import org.example.dbconnectdemo.model.SongList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongListRepository extends JpaRepository<SongList, Long> {
    SongList findByUserOwnerIdAndId(Long UserOwnerId, Long Id);
}
