package com.productservicing.productservice.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/redis-test")
public class RedisTestController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/{key}")
    public String set(@PathVariable String key, @RequestParam String value) {
        redisTemplate.opsForValue().set(key, value);
        return "Saved: " + key + " = " + value;
    }

    @GetMapping("/{key}")
    public String get(@PathVariable String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? value : "No value found for key: " + key;
    }
}