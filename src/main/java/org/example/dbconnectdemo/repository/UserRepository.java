package org.example.dbconnectdemo.repository;

import java.util.Optional;
import org.example.dbconnectdemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    void deleteUserByUsername(String username);
}
