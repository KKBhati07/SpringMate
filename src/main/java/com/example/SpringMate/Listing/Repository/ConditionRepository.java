package com.example.SpringMate.Listing.Repository;

import com.example.SpringMate.Listing.Entity.Condition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConditionRepository extends JpaRepository<Condition, Long> {

    Optional<Condition> findByCode(String code);
    
    List<Condition> findByActiveTrueOrderBySortOrderAsc();
}
