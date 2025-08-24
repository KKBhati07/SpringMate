package com.example.SpringMate.Listing.Repository;

import com.example.SpringMate.Listing.DTO.FetchListingItemsProjection;
import com.example.SpringMate.Listing.Entity.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    Optional<Listing> findByIdAndDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"category", "seller", "listingImages","location"})
    Optional<Listing> findWithRelationsByIdAndDeletedFalse(Long id);

    List<Listing> findByDeletedFalse();

    Page<Listing> findByDeletedFalse(Pageable pageable);


    @Modifying
    @Query("UPDATE Listing l SET l.deleted = true WHERE l.id = :id")
    void softDeleteById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Listing l SET l.deleted = true WHERE l.seller.id = :userId")
    void softDeleteByUserId(@Param("userId") Long userId);

    List<Listing> findByCategoryIdAndDeletedFalse(Long categoryId);

    Page<Listing> findByCategoryIdAndDeletedFalse(Long categoryId, Pageable pageable);


    @Query("""
                SELECT 
                    l.id AS id,
                    l.title AS title,
                    l.description AS description,
                    l.price AS price,
                    l.postedAt AS postedAt,
                    c.name AS categoryName,
                    (
                        SELECT li.url 
                        FROM ListingImage li 
                        WHERE li.listing = l AND li.isCover = true
                        ORDER BY li.createdAt ASC
                        LIMIT 1
                    ) AS coverImageUrl
                FROM Listing l
                LEFT JOIN l.category c
                WHERE l.deleted = false
                  AND (:categoryId IS NULL OR l.category.id = :categoryId)
                  AND (:minPrice IS NULL OR l.price >= :minPrice)
                  AND (:maxPrice IS NULL OR l.price <= :maxPrice)
            """)
    Page<FetchListingItemsProjection> findAllByFilters(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

}
