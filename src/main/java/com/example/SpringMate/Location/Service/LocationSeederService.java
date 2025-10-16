package com.example.SpringMate.Location.Service;

import com.example.SpringMate.Location.Entity.City;
import com.example.SpringMate.Location.Entity.Country;
import com.example.SpringMate.Location.Entity.State;
import com.example.SpringMate.Location.Repository.CityRepository;
import com.example.SpringMate.Location.Repository.CountryRepository;
import com.example.SpringMate.Location.Repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LocationSeederService {
    private final RestTemplate restTemplate;
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    public String seedLocations() {
        if (countryRepository.count() > 0) {
            return "Locations already seeded — skipping!";
        }

        String url = "https://countriesnow.space/api/v0.1/countries";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        List<Map<String, Object>> countries = (List<Map<String, Object>>) response.getBody().get("data");

        countries.forEach(c -> {
            Country country = new Country();
            country.setName((String) c.get("country"));
            country.setCountryCode((String) c.get("iso2"));
            country = countryRepository.save(Country.builder()
                    .countryCode((String) c.get("iso2"))
                    .name((String) c.get("country"))
                    .build());


            List<Map<String, Object>> states = (List<Map<String, Object>>) c.get("states");
            if (states != null) {
                Country finalCountry = country;
                states.forEach(s -> {
                    State state = stateRepository.save(State.builder()
                            .country(finalCountry)
                            .name((String) s.get("name"))
                            .build());

                    List<String> cities = (List<String>) s.get("cities");
                    if (cities != null) {
                        cities.forEach(cityName -> {
                            cityRepository.save(City.builder().name(cityName).state(state).build());
                        });
                    }
                });
            }
        });

        return "Location data seeded successfully.";
    }


}
