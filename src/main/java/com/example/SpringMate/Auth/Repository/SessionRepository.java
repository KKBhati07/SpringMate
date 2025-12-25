package com.example.SpringMate.Auth.Repository;

import com.example.SpringMate.Auth.Entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {
    Optional<Session> findBySessionId(String sessionId);

    @Modifying
    @Query("update Session s set s.lastAccessedAt = :lastAccessedAt where s.sessionId = :sessionId")
    void updateLastAccessedAt(@Param("sessionId") String sessionId, @Param("lastAccessedAt") LocalDateTime lastAccessedAt);

    List<Session> findByUserId(Long userID);
}
