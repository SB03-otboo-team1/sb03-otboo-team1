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
@Order(110)
@RequiredArgsConstructor
public class FeedLikesTableSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "feed_likes")) {
            return;
        }

        List<String> users = fetchIds(jdbcTemplate, "users");
        List<String> feeds = fetchIds(jdbcTemplate, "feeds");
        if (users.isEmpty() || feeds.isEmpty()) {
            return;
        }

        Set<String> used = new HashSet<>();
        List<UUID> userList = new ArrayList<>();
        for (String u : users) {
            userList.add(UUID.fromString(u));
        }
        List<UUID> feedList = new ArrayList<>();
        for (String f : feeds) {
            feedList.add(UUID.fromString(f));
        }

        int created = 0;
        while (created < 10 && used.size() < users.size() * feeds.size()) {
            UUID u = userList.get(randInt(0, userList.size() - 1));
            UUID f = feedList.get(randInt(0, feedList.size() - 1));
            String key = u + ":" + f;
            if (used.add(key)) {
                jdbcTemplate.update(
                    "INSERT INTO feed_likes (id, user_id, feed_id, created_at) VALUES (?,?,?,now())",
                    uuid(), u, f
                );
                created++;
            }
        }
        log.info("FeedLikesTableSeeder: {}개의 피드 좋아요 더미 데이터가 추가되었습니다.", created);
    }
}
