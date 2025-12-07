package com.example.SpringMate.Auth.Controller;

import com.example.SpringMate.Auth.DTO.AuthDetailsResponseDto;
import com.example.SpringMate.Auth.DTO.OtpLoginResponseDto;
import com.example.SpringMate.Auth.DTO.OtpRequestDto;
import com.example.SpringMate.Auth.DTO.OtpLoginRequestDto;
import com.example.SpringMate.Config.JwtTokenProvider;
import com.example.SpringMate.Shared.Exception.BadRequestException;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Auth.Service.AuthService;
import com.example.SpringMate.Shared.Urls;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.Auth.AUTH_BASE)
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping(Urls.Auth.LOGOUT)
    public ResponseEntity<Response<Map<String, Boolean>>>
    logout(
            @CookieValue(value = "auth_token", required = false) String authCookie,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletResponse response
    ) {
        String token = null;
        if (authCookie != null && !authCookie.isBlank()) {
            token = authCookie;
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.split(" ")[1];
        }

        if (token == null || token.isBlank()) {
            throw new BadRequestException("Missing authentication token");
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadRequestException("Invalid Token");
        }

        Map<String, Boolean> res = authService.logoutUser(jwtTokenProvider.getSessionIdFromToken(token));
        ResponseCookie deleteCookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        return ResponseEntity.ok(
                new Response<>(res,
                        "Logged out successfully"));
    }

    @GetMapping(Urls.Auth.AUTH_DETAILS)
    public ResponseEntity<Response<AuthDetailsResponseDto>>
    getAuthDetails(@AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(
                new Response<>(authService.authDetails(authenticatedUser)
                        , "Data fetched successfully"));
    }

    @PostMapping(Urls.Auth.REQUEST_LOGIN_OTP)
    public ResponseEntity<Response<Object>>
    requestLoginOTP(@Valid @RequestBody OtpRequestDto otpRequestDTO)
            throws MessagingException {
        authService.generateAndSendOTP(otpRequestDTO);

        // Always return a generic message (for security)
        return ResponseEntity.ok(
                new Response<>(null,
                        "If your account exists, an OTP has been sent"));

    }

    @PostMapping(Urls.Auth.OTP_LOGIN)
    public ResponseEntity<Response<OtpLoginResponseDto>> loginWithOTP(
            @Valid @RequestBody OtpLoginRequestDto loginDTO,
            HttpServletRequest request) {
        return ResponseEntity.ok(new Response<>(
                authService.verifyOtp(loginDTO, request),
                "Logged in successfully!"));
    }


}
