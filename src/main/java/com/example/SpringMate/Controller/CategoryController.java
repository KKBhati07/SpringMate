package com.example.SpringMate.Controller;

import com.example.SpringMate.Service.CategoryService;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Util.Urls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping(Urls.Category.CATEGORY_BASE)
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping(Urls.Category.GET_ALL)
    public ResponseEntity<Response> getCategories() {
        return ResponseEntity.ok(categoryService.getAllCategory());
    }
}
