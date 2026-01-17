package com.example.SpringMate.Auth.Controller;

import com.example.SpringMate.Auth.DTO.AuthDetailsResponseDto;
import com.example.SpringMate.Auth.DTO.OtpLoginResponseDto;
import com.example.SpringMate.Auth.DTO.OtpRequestDto;
import com.example.SpringMate.Auth.DTO.OtpLoginRequestDto;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.Auth.jwt.JwtTokenProvider;
import com.example.SpringMate.Util.AuthenticatedUser;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Auth.Service.AuthService;
import com.example.SpringMate.Shared.Urls;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.Auth.BASE)
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthHelper authHelper;

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
    requestLoginOTP(@Valid @RequestBody OtpRequestDto otpRequestDTO)
            throws MessagingException {
        log.info(
                "action=REQUEST_LOGIN_OTP identifierType={}",
                otpRequestDTO.getType()
        );

        authService.generateAndSendOTP(otpRequestDTO);

        // Always return a generic message (for security)
        return ResponseEntity.ok(
                Response.success(null,
                        "If your account exists, an OTP has been sent"));

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


}
