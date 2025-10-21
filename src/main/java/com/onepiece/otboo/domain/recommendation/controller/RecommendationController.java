package com.onepiece.otboo.domain.recommendation.controller;

import com.onepiece.otboo.domain.recommendation.controller.api.RecommendationApi;
import com.onepiece.otboo.domain.recommendation.dto.data.RecommendationDto;
import com.onepiece.otboo.domain.recommendation.service.RecommendationService;
import com.onepiece.otboo.global.util.SecurityUtil;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController implements RecommendationApi {

    private final RecommendationService recommendationService;

    @Override
    @GetMapping
    public ResponseEntity<RecommendationDto> getRecommendation(
        @NotNull @RequestParam UUID weatherId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = SecurityUtil.requireAuthorizedUser(auth);

        log.info("추천 조회 API 호출 - weatherId: {}, userId: {}", weatherId, userId);

        RecommendationDto result = recommendationService.getRecommendation(weatherId, userId);

        log.debug("추천 조회 API 호출 완료 - {}", result);

        return ResponseEntity.ok(result);
    }

}
