package com.onepiece.otboo.domain.recommendation.dto.data;

import com.onepiece.otboo.domain.feed.dto.response.OotdDto;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public record RecommendationDto(

    @NotBlank(message = "날씨 정보는 빈 데이터일 수 없습니다")
    UUID weatherId,

    @NotBlank(message = "사용자 정보는 빈 데이터일 수 없습니다")
    UUID userId,

    List<OotdDto> clothes
) {

}
