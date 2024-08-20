package com.example.SpringMate.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <T> T get(String key, Class<T> pojo) {
        Object o = redisTemplate.opsForValue().get(key);
        try {
            if(o == null){throw new Exception("Key Not Found");}
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(o.toString(),pojo);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean set(String key, Object value, Long timeToLive) {
        try{
            ObjectMapper mapper = new ObjectMapper();
            redisTemplate.opsForValue().set(key,mapper.writeValueAsString(value),timeToLive, TimeUnit.SECONDS);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
