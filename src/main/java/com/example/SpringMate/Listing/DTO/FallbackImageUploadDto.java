package com.example.SpringMate.Listing.DTO;

import lombok.Data;

import java.util.List;

@Data
public class FallbackImageUploadDto {
    List<ImageDto> images;
    Long listingId;
}
