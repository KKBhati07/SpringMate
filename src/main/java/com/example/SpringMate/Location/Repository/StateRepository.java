package com.example.SpringMate.Location.Repository;

import com.example.SpringMate.Location.Entity.Country;
import com.example.SpringMate.Location.Entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StateRepository extends JpaRepository<State, Long> {

    Optional<State> findByNameIgnoreCaseAndCountry(String name, Country country);

    List<State> findAllByCountryIdOrderByNameAsc(Long countryId);

}
