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
@Order(170)
@RequiredArgsConstructor
public class NotificationsTableSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "notifications")) {
            return;
        }

        List<String> users = fetchIds(jdbcTemplate, "users");
        if (users.isEmpty()) {
            return;
        }
        int count = 0;
        for (int i = 1; i <= 10; i++) {
            String level = switch (i % 3) {
                case 0 -> "INFO";
                case 1 -> "WARNING";
                default -> "ERROR";
            };
            UUID receiver = UUID.fromString(users.get((i - 1) % users.size()));
            jdbcTemplate.update(
                "INSERT INTO notifications (id, level, title, content, receiver_id, created_at) VALUES (?,?,?,?,?,now())",
                uuid(), level, "Notice " + i, "Notification content " + randStr(14), receiver
            );
            count++;
        }
        log.info("NotificationsTableSeeder: {}개의 알림 더미 데이터가 추가되었습니다.", count);
    }
}
