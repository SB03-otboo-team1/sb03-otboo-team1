package com.onepiece.otboo.domain.weather.entity;


import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.weather.enums.PrecipitationType;
import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import com.onepiece.otboo.domain.weather.enums.WindSpeedWord;
import com.onepiece.otboo.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "weather_data")
public class Weather extends BaseEntity {

    @Column(name = "forecasted_at")
    private Instant forecastedAt;

    @Column(name = "forecast_at")
    private Instant forecastAt;

    @Column(name = "temperature_current", nullable = false)
    private Double temperatureCurrent;

    @Column(name = "temperature_max")
    private Double temperatureMax;

    @Column(name = "temperature_min")
    private Double temperatureMin;

    @Column(name = "temperature_compared_to_day_before")
    private Double temperatureComparedToDayBefore;

    @Column(name = "sky_status")
    @Enumerated(EnumType.STRING)
    private SkyStatus skyStatus;

    @Column(name = "precipitation_amount")
    private Double precipitationAmount;

    @Column(name = "precipitation_probability")
    private Double precipitationProbability;

    @Column(name = "precipitation_type")
    private PrecipitationType precipitationType;

    @Column(name = "wind_speed")
    private Double windSpeed;

    @Column(name = "wind_speed_as_word")
    private WindSpeedWord windSpeedWord;

    @Column(name = "humidity")
    private Double humidity;

    @Column(name = "humidity_compared_to_day_before")
    private Double humidityComparedToDayBefore;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
}
