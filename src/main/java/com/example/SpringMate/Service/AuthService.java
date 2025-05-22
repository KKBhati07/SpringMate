package com.example.SpringMate.Service;


import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Helpers.AuthHelper;
import com.example.SpringMate.Repositoy.SessionRepository;
import com.example.SpringMate.Repositoy.UserRepository;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Util.ResponseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    @Autowired
    public AuthService(UserRepository userRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    public Response logoutUser(){
        Map<String, Object> responseMap = new HashMap<>();
        try{
            Authentication authentication  = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();
            List<Session> sessions = sessionRepository.findByUserId(user.getId());
            sessionRepository.deleteAll(sessions);
            SecurityContextHolder.clearContext();
            responseMap.put("status", 200);
            return new Response(responseMap,"Logged out successfully");
        }catch (Exception e){
            e.printStackTrace();
            responseMap.put("status", 500);
            return new Response(responseMap,"Something went wrong");
        }
    }

    public Response authDetails(){
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", 200);
        responseMap.put("is_authenticated",true);
        responseMap.put("user_details", new ResponseMapper().mapUser(new AuthHelper().getUserDetails()));
        return new Response(responseMap,"Data fetched successfully");
    }
}
