package com.example.SpringMate.Controller;

import com.example.SpringMate.Service.CategoryService;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Util.Urls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequiredArgsConstructor
@RequestMapping(Urls.Category.CATEGORY_BASE)
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping(Urls.Category.GET_ALL)
    public ResponseEntity<Response> getCategories() {
        return categoryService.getAllCategory();
    }
}
