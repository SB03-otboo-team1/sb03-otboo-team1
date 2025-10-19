package com.onepiece.otboo.domain.weather.repository;

import static com.onepiece.otboo.domain.weather.entity.QWeather.weather;

import com.onepiece.otboo.domain.weather.entity.Weather;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WeatherRepositoryCustomImpl implements WeatherRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Weather> findRange(UUID locationId, Instant from, Instant to) {
        return queryFactory
            .selectFrom(weather)
            .where(
                weather.location.id.eq(locationId),
                weather.forecastAt.goe(from),
                weather.forecastAt.loe(to)
            )
            .orderBy(weather.forecastAt.asc())
            .fetch();
    }
}
