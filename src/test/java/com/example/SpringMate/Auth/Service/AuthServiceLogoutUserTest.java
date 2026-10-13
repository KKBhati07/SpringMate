package com.example.SpringMate.Auth.Service;

import com.example.SpringMate.Auth.Cache.AuthCacheService;
import com.example.SpringMate.Auth.Entity.Session;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.Auth.Helper.SessionManagementHelper;
import com.example.SpringMate.Auth.Repository.SessionRepository;
import com.example.SpringMate.Auth.Repository.VerificationCodeRepository;
import com.example.SpringMate.Auth.jwt.JwtTokenProvider;
import com.example.SpringMate.Shared.Exception.BadRequestException;
import com.example.SpringMate.User.Service.CoreUserService;
import com.example.SpringMate.Util.ResponseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.QueryTimeoutException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceLogoutUserTest {

    private static final String SESSION_ID = "test-session-id";

    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private VerificationCodeRepository verificationCodeRepository;
    @Mock
    private CoreUserService coreUserService;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private OtpNotificationDispatcher otpNotificationDispatcher;
    @Mock
    private SessionManagementHelper sessionManagementHelper;
    @Mock
    private AuthCacheService authCacheService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthHelper authHelper;

    @InjectMocks
    private AuthService authService;

    @Test
    void logoutUser_whenSessionPresent_deletesUpdatesAndEvictsInOrder() {
        Session session = Session.builder().sessionId(SESSION_ID).build();
        when(sessionRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.of(session));

        authService.logoutUser(SESSION_ID);

        InOrder inOrder = inOrder(sessionRepository, sessionManagementHelper, authCacheService);
        inOrder.verify(sessionRepository).delete(session);
        inOrder.verify(sessionManagementHelper).updateSessionLogoutTime(SESSION_ID);
        inOrder.verify(authCacheService).evict(SESSION_ID);
    }

    @Test
    void logoutUser_whenSessionAbsent_stillEvictsAndDoesNotDelete() {
        when(sessionRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.empty());

        authService.logoutUser(SESSION_ID);

        verify(sessionRepository, never()).delete(org.mockito.ArgumentMatchers.any());
        verify(authCacheService).evict(SESSION_ID);
    }

    @Test
    void logoutUser_whenEvictFails_propagatesAfterDelete() {
        Session session = Session.builder().sessionId(SESSION_ID).build();
        when(sessionRepository.findBySessionId(SESSION_ID)).thenReturn(Optional.of(session));
        doThrow(new QueryTimeoutException("redis timeout")).when(authCacheService).evict(SESSION_ID);

        assertThrows(QueryTimeoutException.class, () -> authService.logoutUser(SESSION_ID));

        InOrder inOrder = inOrder(sessionRepository, authCacheService);
        inOrder.verify(sessionRepository).delete(session);
        inOrder.verify(authCacheService).evict(SESSION_ID);
    }

    @Test
    void logoutUser_whenSessionIdNull_throwsBadRequestAndDoesNotEvict() {
        assertThrows(BadRequestException.class, () -> authService.logoutUser(null));

        verifyNoInteractions(authCacheService);
        verify(sessionRepository, never()).findBySessionId(org.mockito.ArgumentMatchers.any());
    }
}
