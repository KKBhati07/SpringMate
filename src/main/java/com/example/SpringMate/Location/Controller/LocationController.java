package com.example.SpringMate.Location.Controller;


import com.example.SpringMate.Location.Service.LocationSeederService;
import com.example.SpringMate.Shared.Urls;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController(Urls.Location.LOCATION_BASE)
@RequiredArgsConstructor
public class LocationController {

    private final LocationSeederService locationSeederService;

    @Value("${app.seed-secret}")
    private String seedSecret;


    @PostMapping(Urls.Location.SEED)
    public String seedLocations(@RequestHeader("x-seed-key") String key) {
        if(key == null || key.isEmpty() || !seedSecret.equals(key)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Unauthorized access");
        }
        return locationSeederService.seedLocations();
    }
}
