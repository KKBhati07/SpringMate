package com.example.SpringMate.Helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class AuthHelper {

    public static String failureResponse(String message, String status) throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put("message", message);
        map.put("status", status);
        return new ObjectMapper().writeValueAsString(map);
    }
}
