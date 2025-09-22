package com.onepiece.otboo.domain.weather.repository;

import com.onepiece.otboo.domain.weather.entity.Weather;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WeatherRepositoryCustom {
    List<Weather> findRange(UUID locationId, Instant from, Instant to);
}
