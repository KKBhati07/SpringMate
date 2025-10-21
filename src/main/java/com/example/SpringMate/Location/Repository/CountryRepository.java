package com.example.SpringMate.Location.Repository;

import com.example.SpringMate.Location.Entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByNameIgnoreCase(String name);
    List<Country> findAllByOrderByNameAsc();
    Optional<Country> findByIso2(String iso2);
}
