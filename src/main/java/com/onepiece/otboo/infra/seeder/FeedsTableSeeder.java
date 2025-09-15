package com.onepiece.otboo.infra.seeder;

import static com.onepiece.otboo.infra.seeder.SeedUtils.fetchIds;
import static com.onepiece.otboo.infra.seeder.SeedUtils.hasAny;
import static com.onepiece.otboo.infra.seeder.SeedUtils.randStr;
import static com.onepiece.otboo.infra.seeder.SeedUtils.uuid;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(90)
@RequiredArgsConstructor
public class FeedsTableSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "feeds")) {
            return;
        }

        List<String> users = fetchIds(jdbcTemplate, "users");
        List<String> weathers = fetchIds(jdbcTemplate, "weather_data");
        if (users.isEmpty()) {
            return;
        }

        int count = 0;
        for (int i = 1; i <= 10; i++) {
            UUID id = uuid();
            UUID author = UUID.fromString(users.get((i - 1) % users.size()));
            UUID weather = weathers.isEmpty() ? null
                : UUID.fromString(weathers.get((i - 1) % weathers.size()));
            String content = "Feed content " + i + " - " + randStr(12);
            jdbcTemplate.update(
                "INSERT INTO feeds (id, author_id, weather_id, content, created_at) VALUES (?,?,?,?,now())",
                id, author, weather, content
            );
            count++;
        }
        log.info("FeedsTableSeeder: {}개의 피드 더미 데이터가 추가되었습니다.", count);
    }
}
