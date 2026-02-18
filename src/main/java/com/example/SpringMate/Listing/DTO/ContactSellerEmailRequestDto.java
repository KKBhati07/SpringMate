package com.example.SpringMate.Listing.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactSellerEmailRequestDto {

    @NotBlank(message = "Subject is required")
    @Size(max = 120, message = "Subject must be <= 120 characters")
    private String subject;

    @NotBlank(message = "Body is required")
    @Size(max = 2000, message = "Body must be <= 2000 characters")
    private String body;

    @Size(max = 500, message = "Listing URL must be <= 500 characters")
    @Pattern(
            regexp = "^(https?://.*)?$",
            message = "Listing URL must be a valid http(s) URL"
    )
    private String listingUrl;
}

