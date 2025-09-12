package com.onepiece.otboo.infra.seeder;

import static com.onepiece.otboo.infra.seeder.SeedUtils.*;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class UserTableSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "users")) {
            return;
        }
        int count = 0;
        for (int i = 1; i <= 10; i++) {
            UUID id = uuid();
            String email = "user" + i + "@example.com";
            String password = "!qwe1234";
            jdbcTemplate.update(
                "INSERT INTO users (id, provider, email, password, role, locked, created_at) VALUES (?,?,?,?,?,?,now())",
                id, "LOCAL", email, password, "USER", false
            );
            count++;
        }
        log.info("UserTableSeeder: {}개의 유저 더미 데이터가 추가되었습니다.", count);
    }
}
