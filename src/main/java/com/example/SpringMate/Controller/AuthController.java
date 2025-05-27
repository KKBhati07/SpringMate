package com.example.SpringMate.Controller;

import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Service.AuthService;
import com.example.SpringMate.Util.Urls;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Urls.Auth.AUTH_BASE_URL)
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(Urls.Auth.LOGOUT_URL)
    public ResponseEntity<Response> logout(@AuthenticationPrincipal User user) {
        return authService.logoutUser(user);
    }

    @GetMapping(Urls.Auth.AUTH_DETAILS)
    public ResponseEntity<Response> getAuthDetails(@AuthenticationPrincipal User authenticatedUser) {
        return authService.authDetails(authenticatedUser);
    }
}
