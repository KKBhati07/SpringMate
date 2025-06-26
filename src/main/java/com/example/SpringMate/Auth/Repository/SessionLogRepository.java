package com.example.SpringMate.Auth.Repository;

import com.example.SpringMate.Admin.Entity.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {

    void deleteBySessionId(String sessionId);

    @Modifying
    @Query("update SessionLog s set s.logoutAt = :logoutAt where s.sessionId = :sessionId")
    void updateLogoutTime(@Param("sessionId") String sessionId, @Param("logoutAt")LocalDateTime logoutAt);
}
