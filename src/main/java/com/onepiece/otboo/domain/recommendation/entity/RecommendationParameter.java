package com.onepiece.otboo.domain.recommendation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 추천 알고리즘에 사용되는 변수 테이블.
 *
 */

@Entity
@Table(name = "recommendation_parameter")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecommendationParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    @Column(name = "season_int")
    private Integer seasonInt;

    @Column(name = "sky_status_int")
    private Integer skyStatusInt;

    @Column(name = "max_temp")
    private Double maxTemp;

    @Column(name = "min_temp")
    private Double minTemp;

    @Column(name = "cur_temp")
    private Double curTemp;

    @Column(name = "humidity")
    private Double humidity;

    @Column(name = "wind_speed")
    private Double windSpeed;

    @Column(name = "feel_hot")
    private Double feelHot;

    @Column(name = "feel_cold")
    private Double feelCold;

    @Column(name = "gender_int")
    private Integer genderInt;

    @Column(name = "age")
    private Integer age;

    @Column(name = "temp_sens")
    private Integer tempSens;

}

