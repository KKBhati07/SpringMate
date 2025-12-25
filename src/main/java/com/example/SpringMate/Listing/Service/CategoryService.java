package com.example.SpringMate.Listing.Service;

import com.example.SpringMate.Listing.DTO.FetchCategoriesResponseDto;
import com.example.SpringMate.Listing.Repository.CategoryRepository;
import com.example.SpringMate.Shared.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    // value - a logical bucket inside your cache -> categories::all
    @Cacheable(value = Constants.CacheNamespace.CATEGORY, key = "'all'")
    public FetchCategoriesResponseDto getAllCategory() {
        return new FetchCategoriesResponseDto(categoryRepository.findAll());
    }
}
