package com.example.SpringMate.Auth.Service;

import com.example.SpringMate.Auth.Cache.AuthCacheService;
import com.example.SpringMate.Auth.Cache.CachedAuthentication;
import com.example.SpringMate.Auth.Entity.Session;
import com.example.SpringMate.Auth.Helper.SessionHelper;
import com.example.SpringMate.Auth.Helper.SessionManagementHelper;
import com.example.SpringMate.Auth.Repository.SessionRepository;
import com.example.SpringMate.Shared.Exception.UnauthorizedException;
import com.example.SpringMate.User.Entity.Role;
import com.example.SpringMate.User.Entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionValidationServiceTest {

    private static final String SESSION_ID = "test-session-id";

    @Mock
    private AuthCacheService authCacheService;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private SessionHelper sessionHelper;
    @Mock
    private SessionManagementHelper sessionManagementHelper;

    @InjectMocks
    private SessionValidationService sessionValidationService;

    @Test
    void validate_whenCacheMissAndSessionAbsent_throwsUnauthorized() {
        when(authCacheService.get(SESSION_ID)).thenReturn(null);
        when(sessionRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> sessionValidationService.validate(SESSION_ID));
    }

    @Test
    void validate_whenCacheMissAndValidSession_repopulatesCacheAndReturnsCachedAuth() {
        UUID userUuid = UUID.randomUUID();
        Role role = new Role(1L, "USER");
        User user = User.builder()
                .id(1L)
                .uuid(userUuid)
                .name("Test User")
                .email("test@example.com")
                .role(role)
                .build();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        Session session = Session.builder()
                .sessionId(SESSION_ID)
                .user(user)
                .expiresAt(expiresAt)
                .build();
        CachedAuthentication cached = CachedAuthentication.builder()
                .userId(1L)
                .userUuid(userUuid)
                .email("test@example.com")
                .name("Test User")
                .admin(false)
                .authorities(List.of("ROLE_USER"))
                .build();

        when(authCacheService.get(SESSION_ID)).thenReturn(null, cached);
        when(sessionRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.of(session));

        CachedAuthentication result = sessionValidationService.validate(SESSION_ID);

        assertEquals(cached, result);
        verify(sessionHelper).updateSessionAccessTimestamp(eq(session), any(LocalDateTime.class));
        verify(authCacheService).cacheAuthenticatedUser(SESSION_ID, user, expiresAt);
    }
}
