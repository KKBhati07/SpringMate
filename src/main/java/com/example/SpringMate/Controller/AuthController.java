package com.example.SpringMate.Controller;

import com.example.SpringMate.DTO.OTPRequestDTO;
import com.example.SpringMate.DTO.OtpLoginDTO;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Service.AuthService;
import com.example.SpringMate.Util.Urls;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.Auth.AUTH_BASE_URL)
public class AuthController {

    private final AuthService authService;

    @PostMapping(Urls.Auth.LOGOUT_URL)
    public ResponseEntity<Response> logout(@RequestHeader("sessionid") String sessionId) {
        return authService.logoutUser(sessionId);
    }

    @GetMapping(Urls.Auth.AUTH_DETAILS)
    public ResponseEntity<Response> getAuthDetails(@AuthenticationPrincipal User authenticatedUser) {
        return authService.authDetails(authenticatedUser);
    }

    @PostMapping(Urls.Auth.REQUEST_LOGIN_OTP)
    public ResponseEntity<Response> requestLoginOTP(@Valid @RequestBody OTPRequestDTO otpRequestDTO){
        return authService.generateAndSendOTP(otpRequestDTO);

    }

    @PostMapping(Urls.Auth.OTP_LOGIN)
    public ResponseEntity<Response> loginWithOTP(@Valid @RequestBody OtpLoginDTO loginDTO,
                                                 HttpServletRequest request){
        return authService.verifyOtp(loginDTO, request);

    }

}
