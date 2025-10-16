package com.example.SpringMate.Location.Controller;


import com.example.SpringMate.Location.DTO.CityResponseDto;
import com.example.SpringMate.Location.DTO.CountryResponseDto;
import com.example.SpringMate.Location.DTO.StateResponseDto;
import com.example.SpringMate.Location.Service.LocationSeederService;
import com.example.SpringMate.Location.Service.LocationService;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.Util.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController(Urls.Location.LOCATION_BASE)
@RequiredArgsConstructor
public class LocationController {

    private final LocationSeederService locationSeederService;
    private final LocationService locationService;

    @Value("${spring.application.seed-secret}")
    private String seedSecret;


    @PostMapping(Urls.Location.SEED)
    public String seedLocations(@RequestHeader("x-seed-key") String key) {
        if (key == null || key.isEmpty() || !seedSecret.equals(key)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access");
        }
        return locationSeederService.seedLocations();
    }

    @GetMapping(Urls.Location.GET_COUNTRIES)
    public ResponseEntity<Response<List<CountryResponseDto>>>
    getCountries() {
        return ResponseEntity.ok(
                new Response<>(locationService.getCountries(),
                        "Countries fetched successfully"));
    }

    @GetMapping(Urls.Location.GET_STATES)
    public ResponseEntity<Response<List<StateResponseDto>>>
    getStates(@RequestParam(name = "country_id") long countryId) {
        return ResponseEntity.ok(
                new Response<>(locationService.getStates(countryId),
                        "States fetched successfully"));
    }

    @GetMapping(Urls.Location.GET_CITIES)
    public ResponseEntity<Response<List<CityResponseDto>>>
    getCities(@RequestParam(name = "state_id") long stateId) {
        return ResponseEntity.ok(
                new Response<>(locationService.getCities(stateId),
                        "Cities fetched successfully"));
    }

}
