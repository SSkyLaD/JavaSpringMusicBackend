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
                @UniqueConstraint(columnNames = "username")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends AppUser {

    @OneToOne(cascade = CascadeType.ALL)
    private StorageList userStorageList = new StorageList();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_owner_id", referencedColumnName = "id")
    private List<CustomList> userCustomList = new ArrayList<>();

}
