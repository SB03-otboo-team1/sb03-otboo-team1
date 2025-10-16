package com.onepiece.otboo.domain.recommendation.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;

public class RecommendationNotFoundException extends RecommendationException {

    public RecommendationNotFoundException(String message) {
        super(ErrorCode.RECOMMENDATION_NOT_FOUND, Map.of("message", message));
    }

    public RecommendationNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public RecommendationNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

}
