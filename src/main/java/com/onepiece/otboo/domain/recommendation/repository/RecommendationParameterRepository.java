package com.onepiece.otboo.domain.recommendation.repository;

import com.onepiece.otboo.domain.recommendation.entity.RecommendationParameter;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationParameterRepository extends
    JpaRepository<RecommendationParameter, UUID> {

}
