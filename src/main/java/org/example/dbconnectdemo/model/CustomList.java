package org.example.dbconnectdemo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "CUSTOM_LISTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomList extends SongList{

    private String name;

    @CreationTimestamp
    private Date createDate;
}
