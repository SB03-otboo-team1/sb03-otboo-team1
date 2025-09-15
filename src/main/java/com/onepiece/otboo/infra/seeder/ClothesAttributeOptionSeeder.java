package com.onepiece.otboo.infra.seeder;

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
@Order(60)
@RequiredArgsConstructor
public class ClothesAttributeOptionSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "clothes_attribute_options")) {
            return;
        }

        List<Map<String, Object>> defs = jdbcTemplate.queryForList(
            "SELECT id, name FROM clothes_attribute_defs");
        int count = 0;
        for (Map<String, Object> def : defs) {
            String name = (String) def.get("name");
            UUID defId = (UUID) def.get("id");
            String[] options = switch (name) {
                case "COLOR" -> new String[]{"RED", "BLUE", "GREEN", "BLACK", "WHITE"};
                case "SIZE" -> new String[]{"XS", "S", "M", "L", "XL"};
                case "MATERIAL" -> new String[]{"COTTON", "WOOL", "POLYESTER", "LINEN"};
                default -> new String[]{"N/A"};
            };

            String pick = options[0];
            jdbcTemplate.update(
                "INSERT INTO clothes_attribute_options (id, option_value, definition_id) VALUES (?,?,?)",
                uuid(), pick, defId
            );
            count++;
        }
        log.info("ClothesAttributeOptionSeeder: {}개의 의류 속성 옵션 더미 데이터가 추가되었습니다.", count);
    }
}
