package com.example.SpringMate.User.Repository;

import com.example.SpringMate.User.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUuid(UUID uuid);
    Optional<User> findByEmail(String email);
    boolean existsByUuid(UUID uuid);
    boolean existsByEmail(String email);
    void deleteByUuid(UUID uuid);
}
