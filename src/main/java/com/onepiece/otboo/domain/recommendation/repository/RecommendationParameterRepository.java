package com.onepiece.otboo.domain.recommendation.repository;

import com.onepiece.otboo.domain.recommendation.entity.RecommendationParameter;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationParameterRepository extends
    JpaRepository<RecommendationParameter, UUID> {

    Optional<RecommendationParameter> findByRecommendationId(UUID id);
}
