package com.onepiece.otboo.infra.seeder;

import static com.onepiece.otboo.infra.seeder.SeedUtils.fetchIds;
import static com.onepiece.otboo.infra.seeder.SeedUtils.hasAny;
import static com.onepiece.otboo.infra.seeder.SeedUtils.uuid;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(70)
@RequiredArgsConstructor
public class ClothesAttributesSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "clothes_attributes")) {
            return;
        }

        List<String> clothesIds = fetchIds(jdbcTemplate, "clothes");
        List<Map<String, Object>> defOpt = jdbcTemplate.queryForList(
            "SELECT d.id AS def_id, o.option_value AS opt_val FROM clothes_attribute_defs d JOIN clothes_attribute_options o ON o.definition_id = d.id"
        );
        if (clothesIds.isEmpty() || defOpt.isEmpty()) {
            return;
        }
        int count = 0;
        for (String cIdStr : clothesIds) {
            UUID cId = UUID.fromString(cIdStr);
            for (Map<String, Object> row : defOpt) {
                UUID defId = (UUID) row.get("def_id");
                String optVal = (String) row.get("opt_val");
                jdbcTemplate.update(
                    "INSERT INTO clothes_attributes (id, clothes_id, definition_id, option_value, created_at) VALUES (?,?,?,?,now())",
                    uuid(), cId, defId, optVal
                );
                count++;
            }
        }
        log.info("ClothesAttributesSeeder: {}개의 의류 속성 더미 데이터가 추가되었습니다.", count);
    }
}
