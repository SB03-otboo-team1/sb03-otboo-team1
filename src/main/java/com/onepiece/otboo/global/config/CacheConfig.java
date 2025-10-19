package com.onepiece.otboo.global.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final String PREFIX_CACHE_NAME = "otboo:";

    public static final String CACHE_LOC_BY_ROUNDED_LATLON = "locByRoundedLatLon";
    public static final String CACHE_WEATHERS_BY_DAY = "weathersByDay";

    /**
     * 기본 RedisCacheConfiguration (직렬화/프리픽스/널 미캐싱)
     */
    @Bean
    public RedisCacheConfiguration baseRedisCacheConfiguration(ObjectMapper objectMapper) {
        ObjectMapper redisObjectMapper = objectMapper.copy();

        PolymorphicTypeValidator allowedTypes = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("com.onepiece.otboo.")
            .allowIfSubType("java.time.")
            .allowIfSubType("java.util.")
            .build();

        // 폴리모픽 타입 처리 (허용 패키지만)
        redisObjectMapper.activateDefaultTyping(
            allowedTypes,
            DefaultTyping.NON_FINAL,
            As.PROPERTY
        );

        return RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer(redisObjectMapper)
                )
            )
            .prefixCacheNameWith(PREFIX_CACHE_NAME)
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues();
    }

    /**
     * 캐시 매니저: 캐시별 TTL 지정
     */
    @Bean
    public RedisCacheManager redisCacheManager(
        RedisConnectionFactory connectionFactory,
        RedisCacheConfiguration baseRedisCacheConfiguration
    ) {
        Map<String, RedisCacheConfiguration> perCacheConfigs = new HashMap<>();

        // 위치 캐싱
        perCacheConfigs.put(
            CACHE_LOC_BY_ROUNDED_LATLON,
            baseRedisCacheConfiguration.entryTtl(Duration.ofDays(7))
        );

        // 날씨 일자 단위 캐싱
        perCacheConfigs.put(
            CACHE_WEATHERS_BY_DAY,
            baseRedisCacheConfiguration.entryTtl(Duration.ofHours(3))
        );

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(baseRedisCacheConfiguration) // 기본값(10분)
            .withInitialCacheConfigurations(perCacheConfigs) // 캐시별 TTL 오버라이드
            .transactionAware()
            .build();
    }
}
