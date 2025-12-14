package com.example.SpringMate.Listing.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageDto {
    private MultipartFile image;
    private String objectKey;
    private boolean cover;
}