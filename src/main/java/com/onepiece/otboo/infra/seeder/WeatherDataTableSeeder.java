package com.onepiece.otboo.infra.seeder;

import static com.onepiece.otboo.infra.seeder.SeedUtils.fetchIds;
import static com.onepiece.otboo.infra.seeder.SeedUtils.hasAny;
import static com.onepiece.otboo.infra.seeder.SeedUtils.now;
import static com.onepiece.otboo.infra.seeder.SeedUtils.randDouble;
import static com.onepiece.otboo.infra.seeder.SeedUtils.uuid;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(80)
@RequiredArgsConstructor
public class WeatherDataTableSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "weather_data")) {
            return;
        }

        List<String> users = fetchIds(jdbcTemplate, "users");
        List<String> locations = fetchIds(jdbcTemplate, "locations");
        if (users.isEmpty() || locations.isEmpty()) {
            return;
        }
        int count = 0;
        for (int i = 0; i < 10; i++) {
            UUID id = uuid();
            UUID userId = UUID.fromString(users.get(i % users.size()));
            UUID locId = UUID.fromString(locations.get(i % locations.size()));
            Instant forecastAt = now().minusSeconds(3600L * (i + 1));
            double tcur = randDouble(-10, 35);
            Double tmax = tcur + randDouble(0, 5);
            Double tmin = tcur - randDouble(0, 5);
            String sky = switch (i % 3) {
                case 0 -> "CLEAR";
                case 1 -> "MOSTLY_CLOUDY";
                default -> "CLOUDY";
            };
            String precipType = switch (i % 4) {
                case 0 -> "NONE";
                case 1 -> "RAIN";
                case 2 -> "RAIN_SNOW";
                default -> "SNOW";
            };
            String windWord = switch (i % 3) {
                case 0 -> "WEAK";
                case 1 -> "MODERATE";
                default -> "STRONG";
            };

            jdbcTemplate.update(
                "INSERT INTO weather_data (id, location_id, user_id, forecast_at, temperature_current, temperature_max, temperature_min, sky_status, precipitation_type, wind_speed, wind_speed_as_word, humidity, created_at) "
                    +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,now())",
                id, locId, userId, forecastAt, tcur, tmax, tmin, sky, precipType, randDouble(0, 15),
                windWord, randDouble(10, 90)
            );
            count++;
        }
        log.info("WeatherDataTableSeeder: {}개의 날씨 데이터 더미 데이터가 추가되었습니다.", count);
    }
}
