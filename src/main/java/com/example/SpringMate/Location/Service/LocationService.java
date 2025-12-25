package com.example.SpringMate.Location.Service;

import com.example.SpringMate.Location.DTO.CityResponseDto;
import com.example.SpringMate.Location.DTO.CountryResponseDto;
import com.example.SpringMate.Location.DTO.StateResponseDto;
import com.example.SpringMate.Location.Entity.City;
import com.example.SpringMate.Location.Entity.Country;
import com.example.SpringMate.Location.Entity.Location;
import com.example.SpringMate.Location.Entity.State;
import com.example.SpringMate.Location.Repository.CityRepository;
import com.example.SpringMate.Location.Repository.CountryRepository;
import com.example.SpringMate.Location.Repository.LocationRepository;
import com.example.SpringMate.Location.Repository.StateRepository;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Shared.Exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final CityRepository cityRepository;
    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;
    private final LocationRepository locationRepository;

    public Location getOrCreateOne(Long cityId, Long stateId, Long countryId) {
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new NotFoundException("Country not found"));
        State state = stateRepository.findById(stateId)
                .orElseThrow(() -> new NotFoundException("State not found"));
        City city = this.cityRepository.findById(cityId)
                .orElseThrow(() -> new NotFoundException("City not found"));

        return locationRepository.findByCityAndStateAndCountry(city, state, country)
                .orElseGet(() -> locationRepository.save(
                        Location.builder()
                                .city(city)
                                .state(state)
                                .country(country)
                                .build())
                );
    }

    @Cacheable(value = Constants.CacheNamespace.COUNTRY, key = "'IN_US'")
    public List<CountryResponseDto> getCountries() {
        List<Country> countries = countryRepository
                .findAllByIso2In(List.of("IN", "US"));

        if (countries.isEmpty()) {
            throw new NotFoundException("No countries found");
        }

        return countries.stream()
                .map(c -> CountryResponseDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .toList();
    }

    @Cacheable(value = Constants.CacheNamespace.STATE, key = "#countryId")
    public List<StateResponseDto> getStates(long countryId) {
        log.info("STATE_CACHE_MISS countryId={}", countryId);

        List<State> states = stateRepository.findAllByCountryIdOrderByNameAsc(countryId);

        if (states.isEmpty()) {
            throw new NotFoundException("No states found for given country");
        }

        return states.stream()
                .map(c -> StateResponseDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .toList();
    }

    @Cacheable(value = Constants.CacheNamespace.CITY, key = "#stateId")
    public List<CityResponseDto> getCities(long stateId) {
        log.info("CITY_CACHE_MISS stateId={}", stateId);

        List<City> cities = cityRepository.findAllByStateIdOrderByNameAsc(stateId);

        if (cities.isEmpty()) {
            throw new NotFoundException("No cities found for given state");
        }

        return cities.stream()
                .map(c -> CityResponseDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .toList();

    }
}
