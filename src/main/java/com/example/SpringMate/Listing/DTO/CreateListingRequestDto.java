package com.example.SpringMate.Listing.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateListingRequestDto {

    @NotNull(message = "Title cannot be empty")
    private String title;
    private String description;
    @NotNull(message = "Price cannot be empty")
    private Double price;
    private Long categoryId;
    @NotNull(message = "Must select a condition")
    private Long conditionId;
    @NotNull(message = "City cannot be empty")
    private Long cityId;
    @NotNull(message = "State cannot be empty")
    private Long stateId;
    @NotNull(message = "Country cannot be empty")
    private Long countryId;
    private List<ImageDto> images;

}
