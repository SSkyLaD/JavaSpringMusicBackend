package org.example.dbconnectdemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name ="USERS",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends AppUser {
    private String userDir;

    private double availableMemory = 1024 * 1024 * 1024;

    private int sumOfSongs = 0;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_owner_id", referencedColumnName = "id")
    private List<Song> userSongs = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_owner_id", referencedColumnName = "id")
    private List<SongList> userSongLists = new ArrayList<>();

}
