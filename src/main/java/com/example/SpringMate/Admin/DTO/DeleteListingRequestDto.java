package com.example.SpringMate.Admin.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DeleteListingRequestDto {

    @JsonProperty(value = "listing_ids")
    @NotEmpty(message = "listing_ids must not be empty")
    private List<Long> ids;
}
