package com.example.SpringMate.User.Repository;

import com.example.SpringMate.User.Entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUuid(UUID uuid);
    Optional<User> findByIdAndDeletedFalse(Long id);
    Optional<User> findByUuidAndDeletedFalse(UUID uuid);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndDeletedFalse(String email);
    boolean existsByUuid(UUID uuid);
    boolean existsByEmail(String email);
    @Modifying
    @Query("UPDATE User u SET u.deleted = true WHERE u.uuid = :uuid")
    void softDeleteByUuid(@Param("uuid") UUID uuid);

    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchByNameOrEmailContaining(@Param("search") String search, Pageable pageable);
}
