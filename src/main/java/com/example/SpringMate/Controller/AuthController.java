package com.example.SpringMate.Controller;

import com.example.SpringMate.Response;
import com.example.SpringMate.Urls;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(Urls.Auth.AUTH_BASE_URL)
public class AuthController {
    @PostMapping(Urls.Auth.LOGIN_URL)
    public ResponseEntity<Response> login(HttpServletRequest req){
      Response response = new Response(null,"Login Success");
      return ResponseEntity.ok(response);

    }

    @GetMapping(Urls.Auth.LOGOUT_URL)
    public ResponseEntity<Response> logout(HttpServletRequest req){
        Map<String,Boolean> map =new HashMap<>();
        Response response = new Response(map,"Logout Success");
        return ResponseEntity.ok(response);
    }
}
