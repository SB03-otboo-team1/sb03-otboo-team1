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
@Order(120)
@RequiredArgsConstructor
public class FeedClothesTableSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "feed_clothes")) {
            return;
        }

        List<String> feeds = fetchIds(jdbcTemplate, "feeds");
        List<String> clothes = fetchIds(jdbcTemplate, "clothes");
        if (feeds.isEmpty() || clothes.isEmpty()) {
            return;
        }

        List<UUID> feedList = new ArrayList<>();
        for (String f : feeds) {
            feedList.add(UUID.fromString(f));
        }
        List<UUID> clothesList = new ArrayList<>();
        for (String c : clothes) {
            clothesList.add(UUID.fromString(c));
        }

        Set<String> used = new HashSet<>();
        int created = 0;
        while (created < 10 && used.size() < feeds.size() * clothes.size()) {
            UUID f = feedList.get(randInt(0, feedList.size() - 1));
            UUID c = clothesList.get(randInt(0, clothesList.size() - 1));
            String key = f + ":" + c;
            if (used.add(key)) {
                jdbcTemplate.update(
                    "INSERT INTO feed_clothes (id, feed_id, clothes_id, created_at) VALUES (?,?,?,now())",
                    uuid(), f, c
                );
                created++;
            }
        }
        log.info("FeedClothesTableSeeder: {}개의 피드 의류 더미 데이터가 추가되었습니다.", created);
    }
}
