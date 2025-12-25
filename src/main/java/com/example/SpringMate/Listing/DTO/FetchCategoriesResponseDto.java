package com.example.SpringMate.Listing.DTO;

import com.example.SpringMate.Listing.Entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FetchCategoriesResponseDto {
    List<Category> categories;
}
