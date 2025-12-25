package com.example.SpringMate.Listing.DTO;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListingImageDto {

    private Long id;
    private String url;
    private boolean isCover;

}
