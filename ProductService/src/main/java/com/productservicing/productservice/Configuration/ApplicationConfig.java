package com.productservicing.productservice.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfig {
    @Bean
    public RestTemplate createRestTemplateBean() {
        return  new RestTemplate();
    }
    @Bean
    public RedisTemplate<String,Object> createRedisTemplateBean(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String,Object> redisTemplate =  new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //Default JdkSerializationRedisSerializer requires every cached object to implement Serializable.
        //Use JSON serialization instead so plain model classes (Product, Category, ...) work as-is.
        //enableUnsafeDefaultTyping() embeds the concrete class in the JSON so deserialize() can
        //return a Product instead of a generic LinkedHashMap; safe here since we only ever
        //deserialize values this same app wrote.
        GenericJacksonJsonRedisSerializer jsonSerializer = GenericJacksonJsonRedisSerializer.builder()
                .enableUnsafeDefaultTyping()
                .build();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);
        return redisTemplate;
    }


}
