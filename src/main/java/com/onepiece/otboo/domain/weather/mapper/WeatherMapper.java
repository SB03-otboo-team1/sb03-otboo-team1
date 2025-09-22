package com.onepiece.otboo.domain.weather.mapper;

import com.onepiece.otboo.domain.weather.dto.response.HumidityDto;
import com.onepiece.otboo.domain.weather.dto.response.PrecipitationDto;
import com.onepiece.otboo.domain.weather.dto.response.TemperatureDto;
import com.onepiece.otboo.domain.weather.dto.response.WindSpeedDto;
import com.onepiece.otboo.domain.weather.entity.Weather;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WeatherMapper {

    @Mapping(target = "current", source = "weather.temperatureCurrent")
    @Mapping(target = "comparedToDayBefore", source = "weather.temperatureComparedToDayBefore")
    @Mapping(target = "min", source = "weather.temperatureMin")
    @Mapping(target = "max", source = "weather.temperatureMax")
    TemperatureDto toTemperatureDto(Weather weather);

    @Mapping(target = "type", source = "weather.precipitationType")
    @Mapping(target = "amount", source = "weather.precipitationAmount")
    @Mapping(target = "probability", source = "weather.precipitationProbability")
    PrecipitationDto toPrecipitationDto(Weather weather);

    @Mapping(target = "current", source = "weather.humidity")
    @Mapping(target = "comparedToDayBefore", source = "weather.humidityComparedToDayBefore")
    HumidityDto toHumidityDto(Weather weather);

    @Mapping(target = "speed", source = "weather.windSpeed")
    @Mapping(target = "asWord", source = "weather.windSpeedWord")
    WindSpeedDto toWindSpeedDto(Weather weather);
}
