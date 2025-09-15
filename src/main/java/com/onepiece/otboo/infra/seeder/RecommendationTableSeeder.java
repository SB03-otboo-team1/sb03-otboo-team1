package com.onepiece.otboo.infra.seeder;

import static com.onepiece.otboo.infra.seeder.SeedUtils.fetchIds;
import static com.onepiece.otboo.infra.seeder.SeedUtils.hasAny;
import static com.onepiece.otboo.infra.seeder.SeedUtils.randInt;
import static com.onepiece.otboo.infra.seeder.SeedUtils.uuid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(150)
@RequiredArgsConstructor
public class RecommendationTableSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "recommendation")) {
            return;
        }

        List<String> users = fetchIds(jdbcTemplate, "users");
        List<String> weathers = fetchIds(jdbcTemplate, "weather_data");
        if (users.isEmpty() || weathers.isEmpty()) {
            return;
        }

        List<UUID> userList = new ArrayList<>();
        for (String u : users) {
            userList.add(UUID.fromString(u));
        }
        List<UUID> weatherList = new ArrayList<>();
        for (String w : weathers) {
            weatherList.add(UUID.fromString(w));
        }

        Set<String> used = new HashSet<>();
        int created = 0;
        while (created < 10 && used.size() < users.size() * weathers.size()) {
            UUID u = userList.get(randInt(0, userList.size() - 1));
            UUID w = weatherList.get(randInt(0, weatherList.size() - 1));
            String key = u + ":" + w;
            if (used.add(key)) {
                jdbcTemplate.update(
                    "INSERT INTO recommendation (id, user_id, weather_id, created_at) VALUES (?,?,?,now())",
                    uuid(), u, w
                );
                created++;
            }
        }
        log.info("RecommendationTableSeeder: {}개의 추천 더미 데이터가 추가되었습니다.", created);
    }
}
