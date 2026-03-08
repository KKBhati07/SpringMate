package com.example.SpringMate.Auth.Controller;

import com.example.SpringMate.Auth.DTO.AuthDetailsResponseDto;
import com.example.SpringMate.Auth.DTO.EmailVerificationRequestDto;
import com.example.SpringMate.Auth.DTO.OtpLoginResponseDto;
import com.example.SpringMate.Auth.DTO.OtpRequestDto;
import com.example.SpringMate.Auth.DTO.OtpLoginRequestDto;
import com.example.SpringMate.Auth.DTO.SessionResolveRequestDto;
import com.example.SpringMate.Auth.DTO.SessionResolveResponseDto;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.Auth.jwt.JwtTokenProvider;
import com.example.SpringMate.Util.AuthenticatedUser;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Auth.Service.AuthService;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.Shared.Enum.OTPType;
import com.example.SpringMate.Shared.Exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication and authorization operations.
 * Handles login, logout, OTP flows, and retrieval of authentication details.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.Auth.BASE)
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthHelper authHelper;

    /**
     * Logs out the current user by invalidating their session and clearing auth cookies.
     * Accepts authentication token from either cookie or Authorization header.
     */
    @PostMapping(Urls.Auth.LOGOUT)
    public ResponseEntity<Response<Map<String, Boolean>>>
    logout(
            @CookieValue(value = "auth_token", required = false) String authCookie,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletResponse response
    ) {
        log.info(
                "action=LOGOUT_REQUEST source={}",
                authCookie != null ? "COOKIE" :
                        authHeader != null ? "HEADER" : "NONE"
        );
        String token = null;
        if (authCookie != null && !authCookie.isBlank()) {
            token = authCookie;
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.split(" ")[1];
        }

        if (token != null && !token.isBlank() && jwtTokenProvider.validateToken(token)) {
            String sessionId = jwtTokenProvider.getSessionIdFromToken(token);
            authService.logoutUser(sessionId);
        }

        authHelper.clearAuthCookie(response);
        log.info("action=LOGOUT_SUCCESS sessionInvalidated");
        return ResponseEntity.ok(
                Response.success(Map.of("logged_out", true),
                        "Logged out successfully"));
    }

    @GetMapping(Urls.Auth.AUTH_DETAILS)
    public ResponseEntity<Response<AuthDetailsResponseDto>>
    getAuthDetails(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        log.info(
                "action=FETCH_AUTH_DETAILS userId={}",
                authenticatedUser.uuid()
        );
        return ResponseEntity.ok(
                Response.success(authService.authDetails(authenticatedUser.uuid()),
                        "Data fetched successfully"));
    }

    @PostMapping(Urls.Auth.REQUEST_LOGIN_OTP)
    public ResponseEntity<Response<Object>>
    requestLoginOTP(@Valid @RequestBody OtpRequestDto otpRequestDTO) {
        log.info(
                "action=REQUEST_LOGIN_OTP identifierType={}",
                otpRequestDTO.getType()
        );

        authService.generateAndSendOTP(otpRequestDTO.getEmail(), OTPType.LOGIN);

        // Always return a generic message (for security)
        return ResponseEntity.ok(
                Response.success(null,
                        "If your account exists, an OTP has been sent"));

    }


    @GetMapping(Urls.Auth.REQUEST_EMAIL_VERIFICATION_OTP)
    public ResponseEntity<Response<Object>>
    requestEmailVerificationOTP(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        log.info(
                "action=REQUEST_EMAIL_VERIFICATION_OTP userId={}",
                authenticatedUser.uuid()
        );

        authService.generateAndSendOTP(
                authenticatedUser.email(),
                OTPType.EMAIL_VERIFICATION
        );

        return ResponseEntity.ok(
                Response.success(null,
                        "Email verification OTP has been sent"));

    }

    @PostMapping(Urls.Auth.VERIFY_EMAIL_VERIFICATION_OTP)
    public ResponseEntity<Response<Map<String, Boolean>>> verifyEmailVerificationOTP(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody EmailVerificationRequestDto requestDto
    ) {
        log.info("action=VERIFY_EMAIL_VERIFICATION_OTP_ATTEMPT userId={}", authenticatedUser.uuid());
        if (!authenticatedUser.email().equalsIgnoreCase(requestDto.getEmail())) {
            log.warn("action=VERIFY_EMAIL_VERIFICATION_OTP result=EMAIL_MISMATCH");
            throw new ForbiddenException("Cannot verify another user's email");
        }

        authService.verifyEmailVerificationOtp(requestDto.getEmail(), requestDto.getOtp());

        log.info("action=VERIFY_EMAIL_VERIFICATION_OTP_SUCCESS");
        return ResponseEntity.ok(
                Response.success(Map.of("verified", true), "Email verified successfully")
        );
    }

    @PostMapping(Urls.Auth.OTP_LOGIN)
    public ResponseEntity<Response<OtpLoginResponseDto>> loginWithOTP(
            @Valid @RequestBody OtpLoginRequestDto loginDTO,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("action=OTP_LOGIN_ATTEMPT");

        OtpLoginResponseDto res =
                authService.verifyOtp(loginDTO, request, response);

        log.info(
                "action=OTP_LOGIN_SUCCESS [UUID {}]",
                res.getUserUuid()
        );

        return ResponseEntity.ok(
                Response.success(res, "Logged in successfully!")
        );
    }

    /**
     * Resolves a session by sessionId and returns the associated user UUID.
     * Used by external services (e.g., chat service) to validate sessions.
     */
    @PostMapping(Urls.Auth.RESOLVE_SESSION)
    public ResponseEntity<Response<SessionResolveResponseDto>> resolveSession(
            @Valid @RequestBody SessionResolveRequestDto requestDto
    ) {
        log.info("action=RESOLVE_SESSION_REQUEST sessionId={}", requestDto.getSessionId());

        SessionResolveResponseDto res = authService.resolveSession(requestDto);

        log.info(
                "action=RESOLVE_SESSION_SUCCESS userUuid={}",
                res.getUserUuid()
        );

        return ResponseEntity.ok(
                Response.success(res, "Session resolved successfully")
        );
    }


}
