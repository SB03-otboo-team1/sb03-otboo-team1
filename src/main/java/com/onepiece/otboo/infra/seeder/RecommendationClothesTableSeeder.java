package com.onepiece.otboo.infra.seeder;

import static com.onepiece.otboo.infra.seeder.SeedUtils.*;

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
@Order(160)
@RequiredArgsConstructor
public class RecommendationClothesTableSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "recommendation_clothes")) {
            return;
        }

        List<String> recos = fetchIds(jdbcTemplate, "recommendation");
        List<String> clothes = fetchIds(jdbcTemplate, "clothes");
        if (recos.isEmpty() || clothes.isEmpty()) {
            return;
        }

        List<UUID> recoList = new ArrayList<>();
        for (String r : recos) {
            recoList.add(UUID.fromString(r));
        }
        List<UUID> clothesList = new ArrayList<>();
        for (String c : clothes) {
            clothesList.add(UUID.fromString(c));
        }

        Set<String> used = new HashSet<>();
        int created = 0;
        while (created < 10 && used.size() < recos.size() * clothes.size()) {
            UUID r = recoList.get(randInt(0, recoList.size() - 1));
            UUID c = clothesList.get(randInt(0, clothesList.size() - 1));
            String key = r + ":" + c;
            if (used.add(key)) {
                jdbcTemplate.update(
                    "INSERT INTO recommendation_clothes (id, clothes_id, recommendation_id) VALUES (?,?,?)",
                    uuid(), c, r
                );
                created++;
            }
        }
        log.info("RecommendationClothesTableSeeder: {}개의 추천 의류 더미 데이터가 추가되었습니다.", created);
    }
}
