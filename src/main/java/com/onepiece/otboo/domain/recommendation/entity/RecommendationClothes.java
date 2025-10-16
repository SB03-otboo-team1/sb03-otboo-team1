package com.onepiece.otboo.domain.recommendation.entity;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recommendation_clothes")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecommendationClothes {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @JoinColumn(name = "recommendation_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Recommendation recommendation;

    @JoinColumn(name = "clothes_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Clothes clothes;

}
