package com.example.SpringMate.Location.Controller;


import com.example.SpringMate.Location.DTO.CityResponseDto;
import com.example.SpringMate.Location.DTO.CountryResponseDto;
import com.example.SpringMate.Location.DTO.StateResponseDto;
import com.example.SpringMate.Location.Service.LocationSeederService;
import com.example.SpringMate.Location.Service.LocationService;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.Util.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController()
@RequestMapping(Urls.Location.BASE)
@RequiredArgsConstructor
public class LocationController {

    private final LocationSeederService locationSeederService;
    private final LocationService locationService;

    @Value("${spring.application.seed-secret}")
    private String seedSecret;

    @Value("${spring.application.location-api-key}")
    private String locationApikey;


    @PostMapping(Urls.Location.SEED)
    public String seedLocations(@RequestHeader("x-seed-key") String key) {
        if (key == null || key.isEmpty() || !seedSecret.equals(key)) {
            log.warn("LOCATION_SEED_FORBIDDEN invalid seed key");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access");
        }
        log.info("LOCATION_SEED_STARTED");

        String res = locationSeederService.seedLocations(locationApikey);

        log.info("LOCATION_SEED_COMPLETED");
        return res;
    }

    @GetMapping(Urls.Location.GET_COUNTRIES)
    public ResponseEntity<Response<List<CountryResponseDto>>>
    getCountries() {
        return ResponseEntity.ok(
                Response.success(locationService.getCountries(),
                        "Countries fetched successfully"));
    }

    @GetMapping(Urls.Location.GET_STATES)
    public ResponseEntity<Response<List<StateResponseDto>>>
    getStates(@RequestParam(name = "country_id") long countryId) {
        return ResponseEntity.ok(
                Response.success(locationService.getStates(countryId),
                        "States fetched successfully"));
    }

    @GetMapping(Urls.Location.GET_CITIES)
    public ResponseEntity<Response<List<CityResponseDto>>>
    getCities(@RequestParam(name = "state_id") long stateId) {
        return ResponseEntity.ok(
                Response.success(locationService.getCities(stateId),
                        "Cities fetched successfully"));
    }

}
