package com.example.SpringMate.Listing.DTO;

import java.time.LocalDateTime;

public interface FetchListingItemsProjection {

    Long getId();
    String getTitle();
    String getDescription();
    Double getPrice();
    LocalDateTime getPostedAt();
    String getCategoryName();
    String getCoverImageUrl();
}
