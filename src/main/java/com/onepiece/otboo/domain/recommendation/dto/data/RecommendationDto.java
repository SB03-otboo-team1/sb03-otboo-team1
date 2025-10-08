package com.onepiece.otboo.domain.recommendation.dto.data;

import com.onepiece.otboo.domain.feed.dto.response.OotdDto;
import java.util.UUID;

public record RecommendationDto(
    UUID weatherId,
    UUID userId,
    OotdDto clothes
) {

}
