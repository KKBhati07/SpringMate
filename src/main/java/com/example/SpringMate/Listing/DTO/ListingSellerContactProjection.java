package com.example.SpringMate.Listing.DTO;

import java.util.UUID;

public interface ListingSellerContactProjection {
    Long getListingId();
    String getListingTitle();
    Long getSellerId();
    UUID getSellerUuid();
    String getSellerEmail();
}

