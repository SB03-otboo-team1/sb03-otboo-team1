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
@Order(100)
@RequiredArgsConstructor
public class FeedCommentsTableSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "feed_comments")) {
            return;
        }

        List<String> users = fetchIds(jdbcTemplate, "users");
        List<String> feeds = fetchIds(jdbcTemplate, "feeds");
        if (users.isEmpty() || feeds.isEmpty()) {
            return;
        }

        int count = 0;
        for (int i = 1; i <= 10; i++) {
            UUID id = uuid();
            UUID author = UUID.fromString(users.get((i - 1) % users.size()));
            UUID feed = UUID.fromString(feeds.get((i - 1) % feeds.size()));
            String content = "Comment " + i + " - " + randStr(8);
            jdbcTemplate.update(
                "INSERT INTO feed_comments (id, author_id, feed_id, content, created_at) VALUES (?,?,?,?,now())",
                id, author, feed, content
            );
            count++;
        }
        log.info("FeedCommentsTableSeeder: {}개의 피드 댓글 더미 데이터가 추가되었습니다.", count);
    }
}
