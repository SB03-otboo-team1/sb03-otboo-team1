package com.onepiece.otboo.domain.recommendation.controller;

import com.onepiece.otboo.domain.recommendation.api.RecommendationApi;
import com.onepiece.otboo.domain.recommendation.dto.data.RecommendationDto;
import com.onepiece.otboo.domain.recommendation.service.RecommendationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController implements RecommendationApi {

    private final RecommendationService recommendationService;

    @Override
    @GetMapping
    public ResponseEntity<List<RecommendationDto>> getRecommendations(UUID weatherId) {

        UUID userId = UUID.fromString(
            SecurityContextHolder.getContext().getAuthentication().getName());

        log.info("의상 수정 API 호출 - weatherId: {}, userId: {}", weatherId, userId);

        List<RecommendationDto> result = recommendationService.getRecommendations(weatherId,
            userId);

        return ResponseEntity.ok(result);
    }

}
