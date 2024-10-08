package com.example.SpringMate.Helpers;

import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Repositoy.SessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class AuthHelper {


    private static SessionRepository sessionRepository;

    public static String failureResponse(String message, String status) throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put("message", message);
        map.put("status", status);
        return new ObjectMapper().writeValueAsString(map);
    }

    public static void setSessionRepository(SessionRepository sessionRepository) {
        AuthHelper.sessionRepository = sessionRepository;
    }

    public static boolean updateSession(Session session){
        try{
            session.setLastAccessedAt(System.currentTimeMillis());
            sessionRepository.save(session);
            return true;

        }catch (Exception e){
            return false;
        }
    }
}
