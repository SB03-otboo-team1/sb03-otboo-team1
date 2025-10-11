package com.onepiece.otboo.domain.recommendation.entity;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.UUID;

public class RecommendationClothes {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @JoinColumn(name = "recommendation_id", nullable = false)
    @ManyToOne
    private Recommendation recommendation;

    @JoinColumn(name = "clothes_id", nullable = false)
    @OneToMany(mappedBy = "recommendationClothes", orphanRemoval = true, fetch = jakarta.persistence.FetchType.LAZY)
    private Clothes clothes;

}
