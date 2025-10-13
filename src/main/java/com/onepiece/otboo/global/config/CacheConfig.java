package com.onepiece.otboo.global.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import java.time.Duration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final String PREFIX_CACHE_NAME = "otboo:";

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {
        ObjectMapper redisObjectMapper = objectMapper.copy();
        PolymorphicTypeValidator allowedTypes = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("com.onepiece.otboo.")
            .allowIfSubType("java.time.")
            .allowIfSubType("java.util.")
            .build();
        redisObjectMapper.activateDefaultTyping(
            allowedTypes,
            DefaultTyping.NON_FINAL,
            As.PROPERTY
        );

        return RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer(redisObjectMapper)
                )
            )
            .prefixCacheNameWith(PREFIX_CACHE_NAME)
            .entryTtl(Duration.ofSeconds(600))
            .disableCachingNullValues();
    }
}
