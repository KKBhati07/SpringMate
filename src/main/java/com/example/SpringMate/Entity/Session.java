package com.example.SpringMate.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "sessions")
public class Session {

    public Session(String sessionID,User user,Long createdAt,Long lastAccessedAt) {
        this.sessionID = sessionID;
        this.user = user;
        this.createdAt = createdAt;
        this.lastAccessedAt=lastAccessedAt;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", unique = true,nullable = false)
    private String sessionID;

    @JoinColumn(name = "user_id")
    @ManyToOne
    User user;

    private Long createdAt;
    private Long lastAccessedAt;
}
