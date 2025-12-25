package com.example.SpringMate.Location.Repository;

import com.example.SpringMate.Location.Entity.City;
import com.example.SpringMate.Location.Entity.Country;
import com.example.SpringMate.Location.Entity.Location;
import com.example.SpringMate.Location.Entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByCityAndStateAndCountry(City city, State state, Country country);
}
