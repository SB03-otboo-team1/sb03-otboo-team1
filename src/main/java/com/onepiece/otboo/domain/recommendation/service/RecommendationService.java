package com.onepiece.otboo.domain.recommendation.service;

import com.onepiece.otboo.domain.recommendation.dto.data.RecommendationDto;
import java.util.List;
import java.util.UUID;

public interface RecommendationService {

    List<RecommendationDto> getRecommendations(UUID weatherId, UUID userId);
}
