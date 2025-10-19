package com.onepiece.otboo.infra.seeder;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AllTableSeederRunner {

    private final List<DataSeeder> seeders;
    private final Environment environment;

    @PostConstruct
    public void runAllSeeders() {
        if (!Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            return; // dev 프로필이 아니면 실행 안 함
        }
        seeders.stream()
            .sorted(Comparator.comparingInt(s -> {
                Order o = s.getClass().getAnnotation(Order.class);
                return o != null ? o.value() : Integer.MAX_VALUE;
            }))
            .forEach(DataSeeder::seed);
    }
}
