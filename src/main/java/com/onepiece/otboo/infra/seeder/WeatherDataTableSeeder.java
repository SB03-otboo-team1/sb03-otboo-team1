package com.onepiece.otboo.infra.seeder;

import static com.onepiece.otboo.infra.seeder.SeedUtils.now;
import static com.onepiece.otboo.infra.seeder.SeedUtils.randDouble;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.repository.LocationRepository;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.enums.PrecipitationType;
import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import com.onepiece.otboo.domain.weather.enums.WindSpeedWord;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(80)
@RequiredArgsConstructor
public class WeatherDataTableSeeder implements DataSeeder {

    private final LocationRepository locationRepository;
    private final WeatherRepository weatherRepository;

    @Override
    @Transactional
    public void seed() {
        if (weatherRepository.count() > 0) {
            return;
        }
        List<Location> locations = locationRepository.findAll();
        if (locations.isEmpty()) {
            return;
        }
        int count = 0;
        for (int i = 0; i < 10; i++) {
            Location location = locations.get(i % locations.size());
            Instant forecastAt = now().minusSeconds(3600L * (i + 1));
            double tcur = randDouble(-10, 35);
            Double tmax = tcur + randDouble(0, 5);
            Double tmin = tcur - randDouble(0, 5);
            SkyStatus sky = SkyStatus.values()[i % SkyStatus.values().length];
            PrecipitationType precipType = PrecipitationType.values()[i
                % PrecipitationType.values().length];
            WindSpeedWord windWord = WindSpeedWord.values()[i % WindSpeedWord.values().length];

            Weather weather = Weather.builder()
                .location(location)
                .forecastAt(forecastAt)
                .temperatureCurrent(tcur)
                .temperatureMax(tmax)
                .temperatureMin(tmin)
                .skyStatus(sky)
                .precipitationType(precipType)
                .windSpeed(randDouble(0, 15))
                .windSpeedWord(windWord)
                .humidity(randDouble(10, 90))
                .build();
            weatherRepository.save(weather);
            count++;
        }
        log.info("WeatherDataTableSeeder: {}개의 날씨 데이터 더미 데이터가 추가되었습니다.", count);
    }
}
