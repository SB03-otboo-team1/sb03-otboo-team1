package com.onepiece.otboo.domain.weather.support;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.enums.*;
import com.onepiece.otboo.global.util.NumberConverter;
import com.onepiece.otboo.infra.api.dto.KmaItem;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public class WeatherFactory {

    public static Optional<Weather> buildWeather(
        ForecastKey key,
        Map<String, KmaItem> bucket,
        Extremes extremes,
        Indices indices,
        Location location,
        LocalDate today,
        LocalDate end
    ) {
        LocalDate d = key.toLocalDate();
        if (d.isBefore(today) || d.isAfter(end)) {
            return Optional.empty();
        }

        Double tmp = KmaParsers.toDouble(bucket.get("TMP"));
        if (tmp == null) {
            return Optional.empty();
        }

        Double tmpPrev = KmaLookup.lookupSameOrNearest(indices.tmpByKey(), d.minusDays(1),
            key.time());
        Double reh = KmaParsers.toDouble(bucket.get("REH"));
        Double rehPrev = KmaLookup.lookupSameOrNearest(indices.rehByKey(), d.minusDays(1),
            key.time());

        Double deltaTmp = (tmpPrev != null) ? NumberConverter.round(tmp - tmpPrev, 1) : 0.0;
        Double deltaReh =
            (reh != null && rehPrev != null) ? NumberConverter.round(reh - rehPrev, 1) : 0.0;

        Double tmx = KmaParsers.resolveTmx(bucket, extremes, key.date());
        Double tmn = KmaParsers.resolveTmn(bucket, extremes, key.date());

        Instant forecastedAt = KmaTime.toKstInstant(bucket.values().iterator().next().baseDate(),
            bucket.values().iterator().next().baseTime());
        Instant forecastAt = KmaTime.toKstInstant(key.date(), key.time());

        Weather w = Weather.builder()
            .forecastedAt(forecastedAt)
            .forecastAt(forecastAt)
            .temperatureCurrent(tmp)
            .temperatureMax(tmx)
            .temperatureMin(tmn)
            .skyStatus(KmaMappers.mapSky(bucket.get("SKY")))
            .precipitationAmount(KmaParsers.parsePcpAmountOrCode(bucket.get("PCP")))
            .precipitationProbability(KmaParsers.toDouble(bucket.get("POP")))
            .precipitationType(KmaMappers.mapPty(bucket.get("PTY")))
            .windSpeed(KmaParsers.parseWsdToDouble(bucket.get("WSD")))
            .windSpeedWord(KmaMappers.mapWindSpeedWord(bucket.get("WSD")))
            .humidity(reh)
            .temperatureComparedToDayBefore(deltaTmp)
            .humidityComparedToDayBefore(deltaReh)
            .location(location)
            .build();

        return Optional.of(w);
    }
}
