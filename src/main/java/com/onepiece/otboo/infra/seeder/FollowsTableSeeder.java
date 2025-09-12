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
@Order(130)
@RequiredArgsConstructor
public class FollowsTableSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "follows")) {
            return;
        }

        List<String> users = fetchIds(jdbcTemplate, "users");
        if (users.size() < 2) {
            return;
        }

        List<UUID> ids = new ArrayList<>();
        for (String u : users) {
            ids.add(UUID.fromString(u));
        }

        Set<String> used = new HashSet<>();
        int created = 0;
        while (created < 10 && used.size() < users.size() * users.size()) {
            UUID follower = ids.get(randInt(0, ids.size() - 1));
            UUID following = ids.get(randInt(0, ids.size() - 1));
            if (follower.equals(following)) {
                continue; // 자기 자신을 팔로우할 수 없음
            }
            String key = follower + ":" + following;
            if (used.add(key)) {
                jdbcTemplate.update(
                    "INSERT INTO follows (id, follower_id, following_id, created_at) VALUES (?,?,?,now())",
                    uuid(), follower, following
                );
                created++;
            }
        }
        log.info("FollowsTableSeeder: {}개의 팔로우 더미 데이터가 추가되었습니다.", created);
    }
}
