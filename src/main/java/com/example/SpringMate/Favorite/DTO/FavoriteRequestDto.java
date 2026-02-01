package com.example.SpringMate.Favorite.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FavoriteRequestDto {

    @JsonProperty("is_favorite")
    private boolean isFavorite;
}
