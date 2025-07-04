package com.example.SpringMate.Auth.Controller;

import com.example.SpringMate.Auth.DTO.AuthDetailsResponseDto;
import com.example.SpringMate.Auth.DTO.OtpLoginResponseDto;
import com.example.SpringMate.Auth.DTO.OtpRequestDto;
import com.example.SpringMate.Auth.DTO.OtpLoginRequestDto;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Auth.Service.AuthService;
import com.example.SpringMate.Shared.Urls;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.Auth.AUTH_BASE)
public class AuthController {

    private final AuthService authService;

    @PostMapping(Urls.Auth.LOGOUT)
    public ResponseEntity<Response<Map<String, Boolean>>> logout(@RequestHeader("sessionid") String sessionId) {
        return authService.logoutUser(sessionId);
    }

    @GetMapping(Urls.Auth.AUTH_DETAILS)
    public ResponseEntity<Response<AuthDetailsResponseDto>> getAuthDetails(@AuthenticationPrincipal User authenticatedUser) {
        return authService.authDetails(authenticatedUser);
    }

    @PostMapping(Urls.Auth.REQUEST_LOGIN_OTP)
    public ResponseEntity<Response<Object>> requestLoginOTP(@Valid @RequestBody OtpRequestDto otpRequestDTO){
        return authService.generateAndSendOTP(otpRequestDTO);

    }

    @PostMapping(Urls.Auth.OTP_LOGIN)
    public ResponseEntity<Response<OtpLoginResponseDto>> loginWithOTP(@Valid @RequestBody OtpLoginRequestDto loginDTO, HttpServletRequest request){
        return authService.verifyOtp(loginDTO, request);

    }

}
