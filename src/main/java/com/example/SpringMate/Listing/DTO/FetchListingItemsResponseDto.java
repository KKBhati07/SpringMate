package com.example.SpringMate.Listing.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchListingItemsResponseDto {

    private Long id;
    private String title;
    private String description;
    private Double price;
    private Boolean isDeleted;
    private LocalDateTime postedAt;
    private CategoryDto category;
    private String coverImageUrl;
    private Boolean isFavorite;
    private LocationDto location;
}
