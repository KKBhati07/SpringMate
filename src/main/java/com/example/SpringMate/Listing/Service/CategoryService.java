package com.example.SpringMate.Listing.Service;

import com.example.SpringMate.Listing.DTO.FetchCategoriesResponseDto;
import com.example.SpringMate.Listing.Repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public FetchCategoriesResponseDto getAllCategory() {
        return new FetchCategoriesResponseDto(categoryRepository.findAll());
    }
}
