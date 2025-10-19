package com.onepiece.otboo.infra.seeder;

import static com.onepiece.otboo.infra.seeder.SeedUtils.fetchIds;
import static com.onepiece.otboo.infra.seeder.SeedUtils.hasAny;
import static com.onepiece.otboo.infra.seeder.SeedUtils.randInt;
import static com.onepiece.otboo.infra.seeder.SeedUtils.randStr;
import static com.onepiece.otboo.infra.seeder.SeedUtils.uuid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(140)
@RequiredArgsConstructor
public class DirectMessagesTableSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "direct_messages")) {
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

        int count = 0;
        for (int i = 1; i <= 10; i++) {
            UUID sender = ids.get(randInt(0, ids.size() - 1));
            UUID receiver = ids.get(randInt(0, ids.size() - 1));
            if (sender.equals(receiver)) {
                i--;
                continue;
            }
            jdbcTemplate.update(
                "INSERT INTO direct_messages (id, content, receiver_id, sender_id, created_at) VALUES (?,?,?,?,now())",
                uuid(), "Hello DM " + i + " - " + randStr(10), receiver, sender
            );
            count++;
        }
        log.info("DirectMessagesTableSeeder: {}개의 쪽지 더미 데이터가 추가되었습니다.", count);
    }
}
