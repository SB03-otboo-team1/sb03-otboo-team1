package com.onepiece.otboo.infra.seeder;

import static com.onepiece.otboo.infra.seeder.SeedUtils.fetchIds;
import static com.onepiece.otboo.infra.seeder.SeedUtils.hasAny;
import static com.onepiece.otboo.infra.seeder.SeedUtils.randInt;
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
@Order(30)
@RequiredArgsConstructor
public class UserProfileTableSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "user_profiles")) {
            return;
        }

        List<String> userIds = fetchIds(jdbcTemplate, "users");
        List<String> locationIds = fetchIds(jdbcTemplate, "locations");

        if (userIds.isEmpty()) {
            return;
        }
        int count = 0;
        for (int i = 0; i < userIds.size(); i++) {
            String userId = userIds.get(i);
            String locationId =
                locationIds.isEmpty() ? null : locationIds.get(i % locationIds.size());
            UUID id = uuid();
            String nickname = "user" + (i + 1);
            String gender = switch (i % 3) {
                case 0 -> "MALE";
                case 1 -> "FEMALE";
                default -> "OTHER";
            };
            Integer tempSensitivity = randInt(1, 5);

            jdbcTemplate.update(
                "INSERT INTO user_profiles (id, user_id, location_id, nickname, gender, temp_sensitivity, created_at) VALUES (?,?,?,?,?,?,now())",
                id, UUID.fromString(userId),
                locationId == null ? null : UUID.fromString(locationId), nickname, gender,
                tempSensitivity
            );
            count++;
        }
        log.info("UserProfileTableSeeder: {}개의 프로필 더미 데이터가 추가되었습니다.", count);
    }
}
