package org.example.dbconnectdemo.repository;

import org.example.dbconnectdemo.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;

public interface songRepository extends JpaRepository<Song, Long> {
}
