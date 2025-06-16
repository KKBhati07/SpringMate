package com.example.SpringMate.Repositoy;

import com.example.SpringMate.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

}
