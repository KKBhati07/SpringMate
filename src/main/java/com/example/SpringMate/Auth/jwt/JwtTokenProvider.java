package com.example.SpringMate.Auth.jwt;

import com.example.SpringMate.Config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT token generation and validation.
 * Uses sessionId as subject to enable session invalidation without token rotation.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final AppProperties appProperties;

    private SecretKey key;

    @PostConstruct
    public void init() {
        String jwtSecret = appProperties.getAuth().getJwt().getSecret();
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String sessionId) {
        int validityDays = appProperties.getAuth().getJwt().getValidityDays();
        LocalDateTime expiryDateTime = LocalDateTime.now().plusDays(validityDays);
        Date expiryDate = Date.from(expiryDateTime.atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .subject(sessionId)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getSessionIdFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }
}
