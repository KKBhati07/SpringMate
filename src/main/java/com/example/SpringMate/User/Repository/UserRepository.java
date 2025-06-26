package com.example.SpringMate.User.Repository;

import com.example.SpringMate.User.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUuid(String uuid);
    Optional<User> findByEmail(String email);
    boolean existsByUuid(String uuid);
    boolean existsByEmail(String uuid);
    void deleteByUuid(String uuid);
}
