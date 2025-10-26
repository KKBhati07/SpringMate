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
    Boolean getIsFavorite();

    LocationProjection getLocation();

    interface LocationProjection {
        CityProjection getCity();
        StateProjection getState();
        CountryProjection getCountry();
    }

    interface CityProjection {
        Long getId();
        String getName();
    }

    interface StateProjection {
        Long getId();
        String getName();
    }

    interface CountryProjection {
        Long getId();
        String getName();
    }
}
