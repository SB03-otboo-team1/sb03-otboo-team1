package com.onepiece.otboo.domain.recommendation.repository;

import com.onepiece.otboo.domain.recommendation.entity.Recommendation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, UUID>,
    RecommendationCustomRepository {

}
