package com.example.SpringMate.Location.Service;

import com.example.SpringMate.Location.Entity.City;
import com.example.SpringMate.Location.Entity.Country;
import com.example.SpringMate.Location.Entity.State;
import com.example.SpringMate.Location.Repository.CityRepository;
import com.example.SpringMate.Location.Repository.CountryRepository;
import com.example.SpringMate.Location.Repository.StateRepository;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Shared.Urls;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationSeederService {
    private final RestTemplate restTemplate;
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    String locationApiKey;
    String[] countriesIso = {"IN"};

    @Transactional
    @CacheEvict(value = {
            Constants.CacheNamespace.COUNTRY,
            Constants.CacheNamespace.STATE,
            Constants.CacheNamespace.CITY},
            allEntries = true) //beforeInvocation = true; to evict before method invocation (default is after invocation)
    public String seedLocations(String locationApiKey) {
        log.info("Location seeding started");

        this.locationApiKey = locationApiKey;
        List<Map<String, Object>> countries =
                getApiResponse(Urls.ExternalApi.Locations.COUNTRIES).getBody();


        if (countries == null) {
            log.warn("No countries returned from external API");
            return "Countries not found!";
        }

        log.info("Fetched {} countries from external API", countries.size());

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

        log.info(
                "LOCATION_SEED_COMPLETED cacheEvicted=[COUNTRY, STATE, CITY]"
        );

        return "Location data seeded successfully.";
    }

    private void seedStatesAndCitiesByCountry(Country country) {
        log.info("Seeding locations for country iso={}", country.getIso2());

        List<Map<String, Object>> states = getApiResponse(
                Urls.ExternalApi.Locations.STATES_BY_COUNTRY
                        .replace("{iso2}", country.getIso2())).getBody();

        if (states == null) {
            log.warn("No states found for country iso={}", country.getIso2());
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
            if (cities == null) {
                log.warn(
                        "No cities found for state={} country={}",
                        state.getIso2(),
                        country.getIso2()
                );
                return;
            }

            cityRepository.saveAll(cities.stream().map(c -> City.builder()
                    .name((String) c.get("name"))
                    .state(state).build()).toList());

            log.debug(
                    "Saved {} cities for state={} country={}",
                    cities.size(),
                    state.getIso2(),
                    country.getIso2()
            );

        });


    }

    private ResponseEntity<List> getApiResponse(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-CSCAPI-KEY", locationApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class
            );
        } catch (Exception ex) {
            log.error("External API call failed url={}", url, ex);
            throw ex;
        }
    }


}
