package com.example.SpringMate.Listing.DTO;

import com.example.SpringMate.Listing.Entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class FetchCategoriesResponseDto {
    List<Category> categories;
}
