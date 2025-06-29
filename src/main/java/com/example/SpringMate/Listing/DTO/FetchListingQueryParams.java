package com.example.SpringMate.Listing.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchListingQueryParams {
    private Long categoryId;
    private Double minPrice;
    private Double maxPrice;
    private int page = 0;
    private int size = 10;
}
