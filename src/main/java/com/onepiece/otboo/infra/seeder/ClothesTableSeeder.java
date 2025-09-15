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
@Order(40)
@RequiredArgsConstructor
public class ClothesTableSeeder implements DataSeeder {

    private static final String[] TYPES = {
        "TOP", "BOTTOM", "DRESS", "OUTER", "UNDERWEAR", "ACCESSORY", "SHOES", "SOCKS", "HAT", "BAG",
        "SCARF", "ETC"
    };

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "clothes")) {
            return;
        }

        List<String> userIds = fetchIds(jdbcTemplate, "users");
        if (userIds.isEmpty()) {
            return;
        }
        int count = 0;
        for (int i = 1; i <= 10; i++) {
            UUID id = uuid();
            String owner = userIds.get(randInt(0, userIds.size() - 1));
            String name = "Item-" + i;
            String type = TYPES[i % TYPES.length];
            String image = "https://example.com/img/" + i + ".png";
            jdbcTemplate.update(
                "INSERT INTO clothes (id, owner_id, name, type, image_url, created_at) VALUES (?,?,?,?,?,now())",
                id, UUID.fromString(owner), name, type, image
            );
            count++;
        }
        log.info("ClothesTableSeeder: {}개의 의류 더미 데이터가 추가되었습니다.", count);
    }
}
