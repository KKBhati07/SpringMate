package com.example.SpringMate.Seeder;

import com.example.SpringMate.Listing.Entity.Category;
import com.example.SpringMate.Listing.Repository.CategoryRepository;
import com.example.SpringMate.Shared.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Autowired
    CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        log.info("Category seeding started");

        Set<String> existingCategoriesSet = categoryRepository.findAll().stream()
                .map(category -> category.getName().toLowerCase())
                .collect(Collectors.toSet());
        List<Category> categoriesToAdd = new ArrayList<>();

        for (String category : Constants.CATEGORIES) {
            if (!existingCategoriesSet.contains(category.toLowerCase())) {
                categoriesToAdd.add(new Category(category));
            }
        }

        if (!categoriesToAdd.isEmpty()) {
            log.info("Category seeding completed addedCount={}", categoriesToAdd.size());
            categoryRepository.saveAll(categoriesToAdd);
        } else {
            log.info("Category seeding skipped no new categories");
        }
    }
}
