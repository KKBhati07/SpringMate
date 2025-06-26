package com.example.SpringMate.Listing.Repository;

import com.example.SpringMate.Listing.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

}
