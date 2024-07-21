package org.example.dbconnectdemo.repository;

import java.util.List;
import java.util.Optional;
import org.example.dbconnectdemo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    void deleteUserByUsername(String username);

    Page<User> findAll(Pageable pageable);

    List<User> findByUsernameContaining(String username,Pageable pageable);
}
