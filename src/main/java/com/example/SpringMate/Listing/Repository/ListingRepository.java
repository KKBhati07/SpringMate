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

    @EntityGraph(attributePaths = {"category", "seller", "listingImages", "location"})
    Optional<Listing> findWithRelationsByIdAndDeletedFalse(Long id);

    List<Listing> findByDeletedFalse();

    Page<Listing> findByDeletedFalse(Pageable pageable);


    @Modifying
    @Query("UPDATE Listing l SET l.deleted = true WHERE l.id = :id")
    void softDeleteById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Listing l SET l.deleted = true WHERE l.id in :ids")
    void softDeleteByIds(@Param("ids") List<Long> ids);

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
                    c AS category,
                    
                    (
                        SELECT li.url 
                        FROM ListingImage li 
                        WHERE li.listing = l AND li.isCover = true
                        ORDER BY li.createdAt ASC
                        LIMIT 1
                    ) AS coverImageUrl,
                    loc AS location,
                    CASE 
                        WHEN :userId IS NOT NULL AND uf.id IS NOT NULL AND uf.isFavorite = true THEN true
                        ELSE false 
                    END AS isFavorite
                FROM Listing l
                LEFT JOIN l.category c
                LEFT JOIN l.location loc
                LEFT JOIN loc.city city
                LEFT JOIN loc.state state
                LEFT JOIN loc.country country
                LEFT JOIN UserFavorite uf 
                    ON uf.listing = l AND (:userId IS NOT NULL AND uf.user.id = :userId)
                WHERE 
                    l.deleted = false
                    AND (:userId IS NULL OR l.seller.id != :userId)
                    AND (:categoryId IS NULL OR l.category.id = :categoryId)
                    AND (:minPrice IS NULL OR l.price >= :minPrice)
                    AND (:maxPrice IS NULL OR l.price <= :maxPrice)
                    AND (:countryId IS NULL OR country.id = :countryId)
                    AND (:stateId IS NULL OR state.id = :stateId)
                    AND (:cityId IS NULL OR city.id = :cityId)
                    AND (
                        :searchString IS NULL
                        OR LOWER(l.title) LIKE LOWER(CONCAT('%', COALESCE(:searchString, ''), '%'))
                        OR LOWER(COALESCE(l.description, '')) LIKE LOWER(CONCAT('%', COALESCE(:searchString, ''), '%'))
                    )

            """)
    Page<FetchListingItemsProjection> findAllByFilters(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("countryId") Long countryId,
            @Param("stateId") Long stateId,
            @Param("cityId") Long cityId,
            @Param("searchString") String searchString,
            Pageable pageable
    );

    @Query("""
                SELECT 
                    l.id AS id,
                    l.title AS title,
                    l.description AS description,
                    l.price AS price,
                    l.postedAt AS postedAt,
                    c AS category,
                    (
                        SELECT li.url 
                        FROM ListingImage li 
                        WHERE li.listing = l AND li.isCover = true
                        ORDER BY li.createdAt ASC
                        LIMIT 1
                    ) AS coverImageUrl,
                    loc AS location,
                    false AS isFavorite
                FROM Listing l
                LEFT JOIN l.category c
                LEFT JOIN l.location loc
                LEFT JOIN loc.city city
                LEFT JOIN loc.state state
                LEFT JOIN loc.country country
                WHERE l.deleted = false
                  AND l.seller.id = :userId
            """)
    Page<FetchListingItemsProjection> findAllByUser(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
                SELECT 
                    l.id AS id,
                    l.title AS title,
                    l.description AS description,
                    l.price AS price,
                    l.postedAt AS postedAt,
                    c AS category,
                    (
                        SELECT li.url 
                        FROM ListingImage li 
                        WHERE li.listing = l AND li.isCover = true
                        ORDER BY li.createdAt ASC
                        LIMIT 1
                    ) AS coverImageUrl,
                    loc AS location,
                    uf.isFavorite AS isFavorite
                FROM Listing l
                LEFT JOIN UserFavorite uf 
                    ON uf.listing = l AND uf.user.id = :userId
                LEFT JOIN l.category c
                LEFT JOIN l.location loc
                LEFT JOIN loc.city city
                LEFT JOIN loc.state state
                LEFT JOIN loc.country country
                WHERE l.deleted = false
                  AND l.seller.id = :userId
                  AND uf.isFavorite = true
            """)
    Page<FetchListingItemsProjection> findFavoritesByUser(
            @Param("userId") Long userId,
            Pageable pageable
    );


}
