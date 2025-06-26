package com.example.SpringMate.Listing.Repository;

import com.example.SpringMate.Listing.Entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByDeletedFalse();
    Optional<Listing> findByIdAndDeletedFalse(Long id);
    List<Listing> findByCategoryIdAndDeletedFalse(Long categoryId);
}
