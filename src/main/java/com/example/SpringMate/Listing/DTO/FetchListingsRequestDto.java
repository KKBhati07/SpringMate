package com.example.SpringMate.Listing.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchListingsRequestDto {
    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("min_price")
    private Double minPrice;

    @JsonProperty("max_price")
    private Double maxPrice;

    @JsonProperty("country_id")
    private Long countryId;

    @JsonProperty("state_id")
    private Long stateId;

    @JsonProperty("city_id")
    private Long cityId;

    @JsonProperty("search")
    private String searchString;

    private int page = 0;
    private int size = 10;
}