package com.onepiece.otboo.infra.seeder;

import static com.onepiece.otboo.infra.seeder.SeedUtils.hasAny;
import static com.onepiece.otboo.infra.seeder.SeedUtils.uuid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(50)
@RequiredArgsConstructor
public class ClothesAttributeDefSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "clothes_attribute_defs")) {
            return;
        }

        String[] names = {"COLOR", "SIZE", "MATERIAL"};
        int count = 0;
        for (String name : names) {
            jdbcTemplate.update(
                "INSERT INTO clothes_attribute_defs (id, name, created_at) VALUES (?,?,now())",
                uuid(), name
            );
            count++;
        }
        log.info("ClothesAttributeDefSeeder: {}개의 의류 속성 정의 더미 데이터가 추가되었습니다.", count);
    }
}
