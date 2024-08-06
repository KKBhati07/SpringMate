package com.example.SpringMate.Repositoy;

import com.example.SpringMate.Entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session,String> {
    Session findBySessionID(String sessionID);
    Session findByUserId(Long userID);
}
