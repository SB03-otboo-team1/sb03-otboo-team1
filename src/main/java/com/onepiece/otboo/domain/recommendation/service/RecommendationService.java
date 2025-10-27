package com.onepiece.otboo.domain.recommendation.service;

import com.onepiece.otboo.domain.recommendation.dto.data.RecommendationDto;
import java.util.UUID;

public interface RecommendationService {

    RecommendationDto getRecommendation(UUID weatherId, UUID userId);
}
