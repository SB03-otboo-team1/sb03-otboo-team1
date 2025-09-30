package com.onepiece.otboo.domain.recommendation.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import java.util.Map;

public class RecommendationException  extends GlobalException {

  public RecommendationException(ErrorCode errorCode) {
    super(errorCode);
  }

  public RecommendationException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
