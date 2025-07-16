package com.example.SpringMate.Location.Service;

import com.example.SpringMate.Location.Entity.City;
import com.example.SpringMate.Location.Entity.Country;
import com.example.SpringMate.Location.Entity.Location;
import com.example.SpringMate.Location.Entity.State;
import com.example.SpringMate.Location.Repository.CityRepository;
import com.example.SpringMate.Location.Repository.CountryRepository;
import com.example.SpringMate.Location.Repository.LocationRepository;
import com.example.SpringMate.Location.Repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final CityRepository cityRepository;
    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;
    private final LocationRepository locationRepository;

    public Location getOrCreateOne(String cityName, String stateName, String countryName) {
        Country country = countryRepository.findByNameIgnoreCase(countryName)
                .orElseGet(() -> countryRepository.save(new Country(countryName)));
        State state = stateRepository.findByNameIgnoreCaseAndCountry(stateName, country)
                .orElseGet(() -> stateRepository.save(new State(stateName, country)));
        City city = this.cityRepository.findByNameIgnoreCaseAndState(cityName, state)
                .orElseGet(() -> cityRepository.save(new City(cityName, state)));

        return locationRepository.findByCityAndStateAndCountry(city, state, country)
                .orElseGet(() -> locationRepository.save(
                        Location.builder()
                                .city(city)
                                .state(state)
                                .country(country)
                                .build())
                );
    }
}
