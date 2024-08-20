package com.example.SpringMate;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisTestService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testRedis() {
        redisTemplate.opsForValue().set("email", "abcd@gmail.com");
        Object email = redisTemplate.opsForValue().get("email");
        assert email != null;
        System.out.println("email =>> " + email.toString());
    }
}
