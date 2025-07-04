package com.example.SpringMate.Listing.Service;

import com.example.SpringMate.Listing.DTO.FetchCategoriesResponseDto;
import com.example.SpringMate.Listing.Entity.Category;
import com.example.SpringMate.Listing.Repository.CategoryRepository;
import com.example.SpringMate.Util.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public ResponseEntity<Response<FetchCategoriesResponseDto>> getAllCategory() {
        try {
            return ResponseEntity.ok(
                    new Response<>(new FetchCategoriesResponseDto(categoryRepository.findAll()),
                            "Categories fetched successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new Response<>( new FetchCategoriesResponseDto(Collections.emptyList()),
                            "Failed to fetch categories"));
        }
    }
}
