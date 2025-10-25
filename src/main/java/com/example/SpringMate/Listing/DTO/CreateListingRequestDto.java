package com.example.SpringMate.Listing.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

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

    @Data
    public static class ImageDto{
        private MultipartFile image;
        private boolean cover;
    }
}
