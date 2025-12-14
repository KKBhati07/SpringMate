package com.example.SpringMate.Listing.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateListingRequestDto {

    @NotNull
    private String title;
    private String description;
    @NotNull
    private Double price;
    private Long categoryId;
    @NotNull
    private Long cityId;
    @NotNull
    private Long stateId;
    @NotNull
    private Long countryId;
    private List<ImageDto> images;

}
