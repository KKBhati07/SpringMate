package com.example.SpringMate.Util;

import com.example.SpringMate.Entity.User;

import java.util.HashMap;
import java.util.Map;

public class ResponseMapper {
    public Map<String, Object> mapUser(User user) {
        Map<String, Object> map = new HashMap<>();
        if(user == null) return map;
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("uuid", user.getUuid());
        map.put("is_admin", user.isAdmin());
        return map;
    }
}
