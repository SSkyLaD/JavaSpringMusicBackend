package org.example.dbconnectdemo.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StorageList extends SongList{
    private String localStorageUrl;

    private double availableMemory = 1024 * 1024 * 1024;

    private int sumOfSongs = 0;
}
