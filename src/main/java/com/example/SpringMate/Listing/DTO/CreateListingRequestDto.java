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
    private String city;
    @NotNull
    private String state;
    @NotNull
    private String country;
    private List<ImageDto> images;

    @Data
    public static class ImageDto{
        private MultipartFile image;
        private boolean isCover;
    }
}
