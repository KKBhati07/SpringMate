package com.example.SpringMate.Listing.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CategoryDto {

    private Long id;
    private String name;
    private String icon;

}
