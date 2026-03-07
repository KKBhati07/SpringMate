package com.example.SpringMate.Health.Controller;

import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.Util.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping(Urls.Health.STATUS)
    public ResponseEntity<Response<Map<String, String>>> status() {
        return ResponseEntity.ok(
                Response.success(Map.of("status", "OK"), "Service is healthy")
        );
    }
}
