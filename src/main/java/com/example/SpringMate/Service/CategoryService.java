package com.example.SpringMate.Service;

import com.example.SpringMate.Repositoy.CategoryRepository;
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

    public ResponseEntity<Response> getAllCategory() {
        try {
            Map<String, Object> map = Map.of("categories", categoryRepository.findAll());
            return ResponseEntity.ok(
                    new Response(map,
                            "Categories fetched successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new Response(Map.of("categories", Collections.emptyList()),
                            "Failed to fetch categories"));
        }
    }
}
