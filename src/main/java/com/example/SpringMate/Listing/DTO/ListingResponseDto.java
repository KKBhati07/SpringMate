package com.example.SpringMate.Listing.DTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ListingResponseDto {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private boolean isSold;
    private LocalDateTime postedAt;
    private CategoryDto category;
    private UserDto seller;
    private LocationDto location;
    private List<ListingImageDto> images;
}
