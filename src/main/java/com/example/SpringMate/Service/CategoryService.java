package com.example.SpringMate.Service;

import com.example.SpringMate.Util.Constants;
import com.example.SpringMate.Util.Response;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class CategoryService {
    public Response getAllCategory() {
        Map<String, Object> map = new HashMap<>();
        map.put("categories", Constants.CATEGORIES);
        return new Response(map, "Categories fetched successfully");
    }
}
