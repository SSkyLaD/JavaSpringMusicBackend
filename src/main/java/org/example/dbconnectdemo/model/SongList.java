package org.example.dbconnectdemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "SONGLISTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SongList{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    private String name;

    private int sumOfSongs = 0;

    @CreationTimestamp
    private Date createDate;

    public SongList(String name){
        this.name = name;
    }

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name = "songlist_song",joinColumns = @JoinColumn(name ="songlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id"),uniqueConstraints = {@UniqueConstraint(
            columnNames = {"songlist_id", "song_id"})})
    protected List<Song> songs = new ArrayList<>();
}
