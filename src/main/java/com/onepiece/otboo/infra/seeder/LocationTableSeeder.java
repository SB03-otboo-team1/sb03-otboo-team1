package com.onepiece.otboo.infra.seeder;

import static com.onepiece.otboo.infra.seeder.SeedUtils.hasAny;
import static com.onepiece.otboo.infra.seeder.SeedUtils.randDouble;
import static com.onepiece.otboo.infra.seeder.SeedUtils.randInt;
import static com.onepiece.otboo.infra.seeder.SeedUtils.uuid;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
public class LocationTableSeeder implements DataSeeder {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        if (hasAny(jdbcTemplate, "locations")) {
            return;
        }
        String[] cities = {"서울특별시", "부산광역시", "인천광역시", "대구광역시", "대전광역시", "광주광역시", "울산광역시", "경기도",
            "강원도", "충청북도", "충청남도", "경상북도", "경상남도", "전라북도", "전라남도", "제주특별자치도"};
        String[] districts = {"강남구", "서초구", "중구", "송파구", "해운대구", "수원시", "춘천시", "동구", "남구", "북구",
            "서구", "광산구", "성남시", "안양시", "청주시", "전주시"};
        String[] neighborhoods = {"역삼동", "잠실동", "서면동", "장안동", "삼성동", "신림동", "관악동", "상계동", "중동",
            "학익동", "우동", "범일동", "신촌동", "덕천동", "용두동", "노형동"};
        int count = 0;
        for (int i = 1; i <= 10; i++) {
            UUID id = uuid();
            double lat = randDouble(33.0, 38.0);
            double lon = randDouble(124.0, 132.0);
            int x = randInt(10, 200);
            int y = randInt(10, 200);
            String city = cities[randInt(0, cities.length - 1)];
            String district = districts[randInt(0, districts.length - 1)];
            String neighborhood = neighborhoods[randInt(0, neighborhoods.length - 1)];
            String locationName = city + " " + district + " " + neighborhood;
            jdbcTemplate.update(
                "INSERT INTO locations (id, latitude, longitude, x_coordinate, y_coordinate, location_names) "
                    +
                    "VALUES (?,?,?,?,?, ?)",
                id, lat, lon, x, y, locationName
            );
            count++;
        }
        log.info("LocationTableSeeder: {}개의 위치 더미 데이터가 추가되었습니다.", count);
    }
}
