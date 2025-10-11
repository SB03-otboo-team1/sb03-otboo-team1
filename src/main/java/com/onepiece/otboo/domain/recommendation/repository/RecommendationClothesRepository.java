package com.onepiece.otboo.domain.recommendation.repository;

import com.onepiece.otboo.domain.recommendation.entity.RecommendationClothes;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationClothesRepository extends
    JpaRepository<UUID, RecommendationClothes> {

}
