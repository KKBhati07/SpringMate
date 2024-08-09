package com.example.SpringMate.Repositoy;

import com.example.SpringMate.Entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session,String> {
    Session findBySessionID(String sessionID);
    List<Session> findByUserId(Long userID);
}
