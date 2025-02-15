package com.example.SpringMate.Repositoy;

import com.example.SpringMate.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUuid(String uuid);
    Optional<User> findByEmail(String email);
    boolean existsByUuid(String uuid);
    void deleteByUuid(String uuid);
}
