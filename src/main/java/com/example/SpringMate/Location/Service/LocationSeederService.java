package com.example.SpringMate.Location.Service;

import com.example.SpringMate.Location.Entity.City;
import com.example.SpringMate.Location.Entity.Country;
import com.example.SpringMate.Location.Entity.State;
import com.example.SpringMate.Location.Repository.CityRepository;
import com.example.SpringMate.Location.Repository.CountryRepository;
import com.example.SpringMate.Location.Repository.StateRepository;
import com.example.SpringMate.Shared.Urls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationSeederService {
    private final RestTemplate restTemplate;
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    String locationApiKey;
    String[] countriesIso = {"IN"};

    public String seedLocations(String locationApiKey) {
        this.locationApiKey = locationApiKey;
        List<Map<String, Object>> countries =
                getApiResponse(Urls.ExternalApi.Locations.COUNTRIES).getBody();

        if (countries == null) return "Countries not found!";
        countryRepository.saveAll(countries.stream()
                .map(c -> Country.builder()
                        .iso2((String) c.get("iso2"))
                        .name((String) c.get("name"))
                        .build())
                .toList());

        for (String iso : countriesIso) {
            Optional<Country> c = countryRepository.findByIso2(iso);
            c.ifPresent(this::seedStatesAndCitiesByCountry);
        }

        return "Location data seeded successfully.";
    }

    private void seedStatesAndCitiesByCountry(Country country) {
        List<Map<String, Object>> states = getApiResponse(
                Urls.ExternalApi.Locations.STATES_BY_COUNTRY
                        .replace("{iso2}", country.getIso2())).getBody();

        if (states == null) {
            System.out.println("States not found!!");
            return;
        }
        states.forEach(s -> {
            State state = stateRepository.save(State.builder()
                    .name((String) s.get("name"))
                    .iso2((String) s.get("iso2"))
                    .country(country)
                    .build());

            String getCitiesUrl = Urls.ExternalApi.Locations.CITIES_BY_STATE
                    .replace("{country_iso2}", country.getIso2())
                    .replace("{state_iso2}", state.getIso2());

            List<Map<String, Object>> cities = getApiResponse(getCitiesUrl).getBody();

            cityRepository.saveAll(cities.stream().map(c -> City.builder()
                    .name((String) c.get("name"))
                    .state(state).build()).toList());


        });


    }

    private ResponseEntity<List> getApiResponse(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-CSCAPI-KEY", locationApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                List.class
        );
    }


}
