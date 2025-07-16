package com.example.SpringMate.Location.Repository;

import com.example.SpringMate.Location.Entity.City;
import com.example.SpringMate.Location.Entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {

    Optional<City> findByNameIgnoreCaseAndState(String name, State state);
}
