package com.example.SpringMate.Controller;

import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Service.AuthService;
import com.example.SpringMate.Util.Urls;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

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

    @GetMapping(Urls.Auth.LOGOUT_URL)
    public ResponseEntity<Response> logout(HttpServletRequest req){
            return ResponseEntity.ok(authService.logoutUser());
    }

    @GetMapping(Urls.Auth.AUTH_DETAILS)
    public ResponseEntity<Response> getAuthDetails(HttpServletRequest req){
        return ResponseEntity.ok(authService.authDetails());
    }
}
