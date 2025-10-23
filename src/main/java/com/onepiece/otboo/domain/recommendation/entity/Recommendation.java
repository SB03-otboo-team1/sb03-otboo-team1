package com.onepiece.otboo.domain.recommendation.entity;

import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.global.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recommendation")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Recommendation extends BaseEntity {

    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "uuid")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @JoinColumn(name = "weather_id", nullable = false, columnDefinition = "uuid")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Weather weather;

}
