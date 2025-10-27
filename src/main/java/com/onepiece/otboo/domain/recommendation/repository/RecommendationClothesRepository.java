package com.onepiece.otboo.domain.recommendation.repository;

import com.onepiece.otboo.domain.recommendation.entity.RecommendationClothes;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationClothesRepository extends
    JpaRepository<RecommendationClothes, UUID> {

    void deleteByClothesId(UUID clothesId);
}
