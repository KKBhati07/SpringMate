package com.example.SpringMate.Repositoy;

import com.example.SpringMate.Entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByIsDeletedFalse();
    Optional<Listing> findByIdAndIsDeletedFalse(Long id);
    List<Listing> findByCategoryIdAndIsDeletedFalse(Long categoryId);
}
