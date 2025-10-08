package com.onepiece.otboo.domain.recommendation.entity;

import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.global.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity(name = "recommendation")
@Getter
@RequiredArgsConstructor
public class Recommendation extends BaseEntity {

    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "uuid")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @JoinColumn(name = "weather_id", nullable = false, columnDefinition = "uuid")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Weather weather;

}
