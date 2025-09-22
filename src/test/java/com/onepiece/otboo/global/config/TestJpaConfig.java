package com.onepiece.otboo.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@TestConfiguration
@EnableJpaAuditing(dateTimeProviderRef = "testDateTimeProvider")
public class TestJpaConfig {

    @PersistenceContext
    private EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }

    @Bean
    public MutableDateTimeProvider testDateTimeProvider() {
        return new MutableDateTimeProvider();
    }

    public static class MutableDateTimeProvider implements DateTimeProvider {

        private final AtomicReference<Instant> nowRef =
            new AtomicReference<>(Instant.parse("2025-01-01T00:00:00Z"));

        public void setNow(Instant now) {
            nowRef.set(now);
        }

        @Override
        public Optional<TemporalAccessor> getNow() {
            return Optional.of(nowRef.get());
        }
    }
}
