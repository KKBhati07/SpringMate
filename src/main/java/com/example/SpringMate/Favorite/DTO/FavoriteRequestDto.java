package com.example.SpringMate.Favorite.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FavoriteRequestDto {

    @JsonProperty("listing_id")
    private Long listingId;
}
