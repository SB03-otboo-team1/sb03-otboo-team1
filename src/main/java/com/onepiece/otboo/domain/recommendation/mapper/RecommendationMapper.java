package com.onepiece.otboo.domain.recommendation.mapper;

import com.onepiece.otboo.domain.recommendation.dto.data.RecommendationDto;
import com.onepiece.otboo.domain.recommendation.entity.Recommendation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    @Mapping(target = "weatherId", source = "weather.id")
    RecommendationDto toDto(Recommendation recommendation);
}
